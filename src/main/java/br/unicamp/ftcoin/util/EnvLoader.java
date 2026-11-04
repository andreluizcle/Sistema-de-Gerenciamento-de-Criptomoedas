package br.unicamp.ftcoin.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Leitor simples de arquivos {@code .env}, sem dependencias externas.
 *
 * <p>Formato aceito: {@code KEY=value}, uma entrada por linha. Linhas em branco
 * e linhas iniciadas por {@code #} sao ignoradas; espacos ao redor da chave e do
 * valor sao descartados e aspas envolvendo o valor sao removidas.</p>
 *
 * <p>Ao resolver uma chave, a variavel de ambiente do sistema tem prioridade
 * sobre o arquivo. O arquivo e procurado em {@code ENV_FILE}, depois
 * {@code ./.env} e por fim {@code ../.env} (util ao executar a partir de
 * {@code out/} ou {@code target/}).</p>
 */
public final class EnvLoader {

    private static final Map<String, String> entries = new HashMap<>();
    private static boolean loaded = false;

    private EnvLoader() {
    }

    /** Carrega o arquivo uma unica vez; chamadas seguintes nao fazem nada. */
    public static synchronized void load() {
        if (loaded) {
            return;
        }
        Path file = locateFile();
        if (file != null) {
            readFile(file);
        }
        loaded = true;
    }

    private static Path locateFile() {
        String configuredPath = System.getenv("ENV_FILE");
        if (configuredPath != null && !configuredPath.isEmpty()) {
            Path customFile = Paths.get(configuredPath);
            if (Files.exists(customFile)) {
                return customFile;
            }
        }
        Path currentDirFile = Paths.get(".env");
        if (Files.exists(currentDirFile)) {
            return currentDirFile;
        }
        Path parentDirFile = Paths.get("..", ".env");
        if (Files.exists(parentDirFile)) {
            return parentDirFile;
        }
        return null;
    }

    private static void readFile(Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line);
            }
        } catch (IOException e) {
            // Sem .env legivel a aplicacao ainda roda em memoria; o modo relacional
            // falha depois, com mensagem propria, ao faltar DB_USER/DB_PASSWORD.
            System.err.println("Warning: could not read " + file + ": " + e.getMessage());
        }
    }

    private static void parseLine(String rawLine) {
        String line = rawLine.trim();
        if (line.isEmpty() || line.startsWith("#")) {
            return;
        }
        int separator = line.indexOf('=');
        if (separator <= 0) {
            return;
        }
        String key = line.substring(0, separator).trim();
        String value = stripQuotes(line.substring(separator + 1).trim());
        entries.put(key, value);
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    /** Valor da chave, ou {@code null}. Variavel de ambiente tem prioridade. */
    public static String get(String key) {
        load();
        String fromSystem = System.getenv(key);
        if (fromSystem != null && !fromSystem.isEmpty()) {
            return fromSystem;
        }
        return entries.get(key);
    }

    /** Valor da chave, ou {@code defaultValue} quando ausente ou vazio. */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }
}
