package br.unicamp.ftcoin.oracle;

import network.ExchangeRate;
import network.NetworkRequest;
import network.NetworkResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Servidor de cotacoes falso, dentro do proprio processo de teste.
 *
 * <p>Fala o protocolo real — os mesmos objetos serializados — em uma porta
 * efemera do loopback. Existe para que a integracao de rede seja testada de
 * verdade, incluindo a serializacao e o formato da resposta, sem depender da
 * rede da faculdade.</p>
 *
 * <p>A porta e escolhida pelo sistema operacional, entao nao ha conflito com
 * outro processo nem com o servidor real. A thread e daemon e o socket fecha no
 * {@code close()}, de modo que nenhum teste deixa recurso para tras.</p>
 */
final class FakeQuoteServer implements AutoCloseable {

    /** Modo de resposta deste servidor. */
    private enum Mode { RESPOND, SILENT, GARBAGE }

    private final ServerSocket serverSocket;
    private final Thread thread;
    private final CountDownLatch shutdown = new CountDownLatch(1);
    private final NetworkResponse response;
    private final Mode mode;

    private volatile NetworkRequest lastRequest;

    private FakeQuoteServer(NetworkResponse response, Mode mode) throws IOException {
        this.response = response;
        this.mode = mode;
        this.serverSocket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        this.thread = new Thread(this::serve, "fake-quote-server");
        this.thread.setDaemon(true);
        this.thread.start();
    }

    /** Responde exatamente este objeto. */
    static FakeQuoteServer replying(NetworkResponse response) throws IOException {
        return new FakeQuoteServer(response, Mode.RESPOND);
    }

    /** Responde com estas cotacoes e sem mensagem de erro. */
    static FakeQuoteServer replying(ExchangeRate... rates) throws IOException {
        return replying(new NetworkResponse(List.of(rates), null));
    }

    /** Aceita a conexao e nao envia nada, forcando o tempo de leitura a esgotar. */
    static FakeQuoteServer silent() throws IOException {
        return new FakeQuoteServer(null, Mode.SILENT);
    }

    /** Responde com um objeto que nao e NetworkResponse. */
    static FakeQuoteServer replyingGarbage() throws IOException {
        return new FakeQuoteServer(null, Mode.GARBAGE);
    }

    int port() {
        return serverSocket.getLocalPort();
    }

    /** O ultimo pedido recebido, para conferir credenciais e intervalo. */
    NetworkRequest lastRequest() {
        return lastRequest;
    }

    private void serve() {
        while (!serverSocket.isClosed()) {
            try (Socket socket = serverSocket.accept()) {
                handle(socket);
            } catch (IOException e) {
                return;   // socket fechado pelo close(): fim normal da thread
            }
        }
    }

    private void handle(Socket socket) throws IOException {
        if (mode == Mode.SILENT) {
            awaitShutdown();
            return;
        }
        // Mesma ordem do servidor real: o cabecalho de saida vai primeiro.
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
        try {
            lastRequest = (NetworkRequest) input.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
        output.writeObject(mode == Mode.GARBAGE ? "isto nao e um NetworkResponse" : response);
        output.flush();
    }

    /** Espera o encerramento sem dormir por tempo fixo, para o teste nao ficar instavel. */
    private void awaitShutdown() {
        try {
            shutdown.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        shutdown.countDown();
        try {
            serverSocket.close();
        } catch (IOException ignored) {
            // Encerrando de qualquer forma.
        }
        try {
            thread.join(1_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
