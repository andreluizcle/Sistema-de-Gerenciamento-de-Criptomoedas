package br.unicamp.ftcoin.oracle;

import br.unicamp.ftcoin.dto.Quote;
import br.unicamp.ftcoin.util.EnvLoader;

import network.ExchangeRate;
import network.NetworkRequest;
import network.NetworkResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cotacoes obtidas do servidor do professor pela API de rede.
 *
 * <p>A API nao e REST e nao usa JSON nem XML: e um servidor TCP que troca
 * objetos Java serializados. O cliente abre um socket, escreve um
 * {@link NetworkRequest} com as credenciais do grupo e o intervalo de datas, e
 * le de volta um {@link NetworkResponse} com a lista de cotacoes ou uma
 * mensagem de erro.</p>
 *
 * <p>Esta classe e o unico ponto do sistema que conhece o pacote {@code
 * network}. As demais camadas trabalham apenas com {@link Quote}, de modo que o
 * protocolo nao vaza para servicos, controladores ou telas.</p>
 *
 * <p>Nenhuma credencial fica no codigo: tudo vem do arquivo {@code .env} pela
 * fabrica {@link #fromEnvironment()}.</p>
 */
public final class ApiQuoteSource implements QuoteSource {

    public static final String DEFAULT_HOST = "143.106.243.64";
    public static final int DEFAULT_PORT = 1234;
    public static final int DEFAULT_TIMEOUT_MS = 5_000;

    private final String host;
    private final int port;
    private final String groupName;
    private final String groupPassword;
    private final int timeoutMillis;

    /**
     * Construtor explicito. E por ele que o teste aponta para um servidor local.
     *
     * @param timeoutMillis limite para conectar e para esperar a resposta
     */
    public ApiQuoteSource(String host, int port, String groupName,
                          String groupPassword, int timeoutMillis) {
        this.host = host;
        this.port = port;
        this.groupName = groupName;
        this.groupPassword = groupPassword;
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * Configuracao de producao, lida do arquivo {@code .env}.
     *
     * <p>Grupo e senha caem para {@code DB_USER} e {@code DB_PASSWORD} quando as
     * chaves proprias do oraculo nao existem, ja que sao as mesmas credenciais;
     * assim um {@code .env} preenchido apenas para o banco continua servindo.
     * Nao abre conexao alguma, portanto nao falha na partida.</p>
     */
    public static ApiQuoteSource fromEnvironment() {
        return new ApiQuoteSource(
                EnvLoader.get("ORACLE_HOST", DEFAULT_HOST),
                intOrDefault("ORACLE_PORT", DEFAULT_PORT),
                EnvLoader.get("ORACLE_GROUP", EnvLoader.get("DB_USER")),
                EnvLoader.get("ORACLE_PASSWORD", EnvLoader.get("DB_PASSWORD")),
                intOrDefault("ORACLE_TIMEOUT_MS", DEFAULT_TIMEOUT_MS));
    }

    /** Um numero invalido no .env nao deve derrubar a partida. */
    private static int intOrDefault(String key, int fallback) {
        try {
            return Integer.parseInt(EnvLoader.get(key, String.valueOf(fallback)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /** Endereco em uso, para exibir na partida. Nunca inclui a senha. */
    public String address() {
        return host + ":" + port;
    }

    /**
     * Cotacao de uma data.
     *
     * @throws QuoteUnavailableException se o servidor nao devolver essa data
     */
    @Override
    public Quote fetch(LocalDate date) {
        for (Quote quote : fetchBetween(date, date)) {
            if (quote.date().equals(date)) {
                return quote;
            }
        }
        throw new QuoteUnavailableException("error.oracle.noQuote");
    }

    /**
     * Todas as cotacoes do intervalo em uma unica requisicao.
     *
     * <p>Consulta de melhor esforco: o servidor pode devolver menos datas do que
     * o pedido, e isso nao e erro. E este metodo que transforma uma apuracao de
     * dezenas de idas a rede em uma so.</p>
     */
    @Override
    public List<Quote> fetchBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            return List.of();
        }
        NetworkResponse response = send(new NetworkRequest(groupName, groupPassword, from, to));
        List<Quote> quotes = new ArrayList<>();
        for (ExchangeRate rate : response.getExchangeRates()) {
            if (rate != null && rate.getDate() != null && rate.getValue() != null) {
                quotes.add(new Quote(rate.getDate(), rate.getValue()));
            }
        }
        return quotes;
    }

    /** Envia o pedido e valida a resposta, traduzindo cada falha em uma chave. */
    private NetworkResponse send(NetworkRequest request) {
        try {
            NetworkResponse response = exchange(request);
            if (response == null) {
                throw new QuoteUnavailableException("error.oracle.protocol");
            }
            // A mensagem de erro e verificada antes da lista. Ao recusar as
            // credenciais o servidor devolve a mensagem E a lista nula, e
            // inverter a ordem faria uma senha errada ser relatada como
            // resposta fora do protocolo.
            String error = response.getErrorMessage();
            if (error != null && !error.isBlank()) {
                // O texto vem do servidor e nao e traduzivel, entao viaja na
                // causa: o usuario ve a mensagem no seu idioma e o texto
                // original fica no registro de execucao.
                throw new QuoteUnavailableException("error.oracle.server", new IOException(error));
            }
            if (response.getExchangeRates() == null) {
                throw new QuoteUnavailableException("error.oracle.protocol");
            }
            return response;
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new QuoteUnavailableException("error.oracle.protocol", e);
        } catch (IOException e) {
            throw new QuoteUnavailableException("error.oracle.network", e);
        }
    }

    /** Uma conexao por requisicao: o servidor responde uma vez e encerra. */
    private NetworkResponse exchange(NetworkRequest request)
            throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMillis);
            // Precisa vir antes de abrir o fluxo de entrada: o construtor do
            // ObjectInputStream ja e uma leitura e bloquearia sem limite.
            socket.setSoTimeout(timeoutMillis);

            // A ordem importa. Abrir o fluxo de entrada antes de enviar o pedido
            // trava o cliente esperando o cabecalho do servidor, que por sua vez
            // espera o pedido. Escrever primeiro e seguro com qualquer servidor.
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(request);
            output.flush();

            try (ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
                return (NetworkResponse) input.readObject();
            }
        }
    }
}
