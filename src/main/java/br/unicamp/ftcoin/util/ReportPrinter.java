package br.unicamp.ftcoin.util;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.PrintException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Envio de relatorios para uma impressora conectada.
 *
 * <p>Usa apenas {@code javax.print}, da biblioteca padrao. Quando nao ha
 * impressora instalada — o caso comum em laboratorio e na gravacao do video —
 * o relatorio e gravado em arquivo de texto, de modo que a funcionalidade
 * sempre produz um resultado verificavel.</p>
 */
public final class ReportPrinter {

    private ReportPrinter() {
    }

    /** Impressoras visiveis para a JVM. Lista vazia se nao houver nenhuma. */
    public static List<PrintService> availablePrinters() {
        return List.of(PrintServiceLookup.lookupPrintServices(null, null));
    }

    /**
     * Envia o texto para a impressora escolhida.
     *
     * @throws PrintException se a impressora recusar o trabalho
     */
    public static void printTo(PrintService printer, String content) throws PrintException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        Doc document = new SimpleDoc(bytes, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
        DocPrintJob job = printer.createPrintJob();
        job.print(document, null);
    }

    /**
     * Grava o relatorio em arquivo de texto no diretorio corrente.
     *
     * @return o caminho do arquivo gravado
     * @throws IOException se nao for possivel gravar
     */
    public static Path saveToFile(String fileName, String content) throws IOException {
        Path file = Paths.get(fileName);
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file.toAbsolutePath();
    }
}
