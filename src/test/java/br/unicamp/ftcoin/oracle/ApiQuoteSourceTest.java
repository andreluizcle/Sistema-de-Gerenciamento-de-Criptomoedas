package br.unicamp.ftcoin.oracle;

import br.unicamp.ftcoin.dto.Quote;
import network.ExchangeRate;
import network.NetworkRequest;
import network.NetworkResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integracao com a API de cotacoes, contra um servidor falso no proprio
 * processo. Nenhum teste toca a rede da faculdade.
 */
@Timeout(10)
class ApiQuoteSourceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 2, 10);
    private static final String GROUP = "POO2_2s26_B03";
    private static final String PASSWORD = "senha-de-teste";
    private static final int TIMEOUT_MS = 300;

    private ApiQuoteSource sourceFor(int port) {
        return new ApiQuoteSource("127.0.0.1", port, GROUP, PASSWORD, TIMEOUT_MS);
    }

    private ExchangeRate rate(LocalDate date, String value) {
        return new ExchangeRate(date, new BigDecimal(value));
    }

    @Test
    @DisplayName("Uma data devolve a cotacao e envia as credenciais do grupo")
    void fetchesASingleDate() throws IOException {
        try (FakeQuoteServer server = FakeQuoteServer.replying(rate(DAY, "11.45"))) {
            Quote quote = sourceFor(server.port()).fetch(DAY);

            assertEquals(DAY, quote.date(), "data da cotacao");
            assertEquals(0, quote.price().compareTo(new BigDecimal("11.45")), "valor");

            NetworkRequest sent = server.lastRequest();
            assertNotNull(sent, "o servidor deveria ter recebido um pedido");
            assertEquals(GROUP, sent.getGroupName(), "nome do grupo");
            assertEquals(PASSWORD, sent.getGroupPassword(), "senha do grupo");
            assertEquals(DAY, sent.getFirstDate(), "data inicial");
            assertEquals(DAY, sent.getLastDate(), "data final");
        }
    }

    @Test
    @DisplayName("Um intervalo traz varias cotacoes em uma unica requisicao")
    void fetchesARangeInOneRequest() throws IOException {
        try (FakeQuoteServer server = FakeQuoteServer.replying(
                rate(DAY, "11.45"), rate(DAY.plusDays(1), "11.90"), rate(DAY.plusDays(2), "12.10"))) {

            List<Quote> quotes = sourceFor(server.port()).fetchBetween(DAY, DAY.plusDays(2));

            assertEquals(3, quotes.size(), "cotacoes recebidas");
            assertEquals(DAY, server.lastRequest().getFirstDate(), "data inicial pedida");
            assertEquals(DAY.plusDays(2), server.lastRequest().getLastDate(), "data final pedida");
        }
    }

    @Test
    @DisplayName("Erro devolvido pelo servidor vira mensagem traduzida, com o texto original na causa")
    void reportsServerSideError() throws IOException {
        NetworkResponse refusal = new NetworkResponse(List.of(), "grupo nao autorizado");
        try (FakeQuoteServer server = FakeQuoteServer.replying(refusal)) {
            ApiQuoteSource source = sourceFor(server.port());

            QuoteUnavailableException failure =
                    assertThrows(QuoteUnavailableException.class, () -> source.fetch(DAY));

            assertEquals("error.oracle.server", failure.getMessageKey());
            assertNotNull(failure.getCause(), "a causa deveria trazer o texto do servidor");
            assertTrue(failure.getCause().getMessage().contains("grupo nao autorizado"),
                    "texto do servidor: " + failure.getCause().getMessage());
        }
    }

    @Test
    @DisplayName("Servidor inacessivel vira erro de rede, sem travar")
    void reportsUnreachableServer() throws IOException {
        int closedPort;
        try (FakeQuoteServer server = FakeQuoteServer.replying()) {
            closedPort = server.port();
        }
        ApiQuoteSource source = sourceFor(closedPort);

        QuoteUnavailableException failure =
                assertThrows(QuoteUnavailableException.class, () -> source.fetch(DAY));

        assertEquals("error.oracle.network", failure.getMessageKey());
    }

    @Test
    @DisplayName("Servidor que aceita e nao responde esgota o tempo de leitura")
    void reportsReadTimeout() throws IOException {
        // Este caso so passa se o tempo limite for definido ANTES de abrir o
        // fluxo de entrada: o construtor do ObjectInputStream ja e uma leitura.
        try (FakeQuoteServer server = FakeQuoteServer.silent()) {
            ApiQuoteSource source = sourceFor(server.port());

            QuoteUnavailableException failure =
                    assertThrows(QuoteUnavailableException.class, () -> source.fetch(DAY));

            assertEquals("error.oracle.network", failure.getMessageKey());
        }
    }

    @Test
    @DisplayName("Lista vazia: o intervalo nao e erro, a data especifica e")
    void handlesEmptyResponse() throws IOException {
        try (FakeQuoteServer server = FakeQuoteServer.replying()) {
            ApiQuoteSource source = sourceFor(server.port());

            assertTrue(source.fetchBetween(DAY, DAY.plusDays(3)).isEmpty(),
                    "intervalo vazio e resposta legitima");

            QuoteUnavailableException failure =
                    assertThrows(QuoteUnavailableException.class, () -> source.fetch(DAY));
            assertEquals("error.oracle.noQuote", failure.getMessageKey());
        }
    }

    @Test
    @DisplayName("Data pedida ausente da resposta vira erro de cotacao indisponivel")
    void reportsMissingDate() throws IOException {
        try (FakeQuoteServer server = FakeQuoteServer.replying(rate(DAY.plusDays(1), "11.90"))) {
            ApiQuoteSource source = sourceFor(server.port());

            QuoteUnavailableException failure =
                    assertThrows(QuoteUnavailableException.class, () -> source.fetch(DAY));

            assertEquals("error.oracle.noQuote", failure.getMessageKey());
        }
    }

    @Test
    @DisplayName("Resposta fora do protocolo vira erro de protocolo")
    void reportsUnexpectedResponse() throws IOException {
        try (FakeQuoteServer server = FakeQuoteServer.replyingGarbage()) {
            ApiQuoteSource source = sourceFor(server.port());

            QuoteUnavailableException failure =
                    assertThrows(QuoteUnavailableException.class, () -> source.fetch(DAY));

            assertEquals("error.oracle.protocol", failure.getMessageKey());
        }
    }

    @Test
    @DisplayName("Intervalo invertido nao gera requisicao")
    void ignoresInvertedRange() throws IOException {
        try (FakeQuoteServer server = FakeQuoteServer.replying()) {
            ApiQuoteSource source = sourceFor(server.port());

            assertTrue(source.fetchBetween(DAY.plusDays(5), DAY).isEmpty());
            assertEquals(null, server.lastRequest(), "nao deveria ter ido ate o servidor");
        }
    }
}
