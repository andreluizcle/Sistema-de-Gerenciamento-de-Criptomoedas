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
 * Carregador simples de arquivos .env (sem dependencias externas).
 *
 * Regras de leitura:
 *   - Linhas em branco e linhas iniciadas por '#' sao ignoradas;
 *   - Formato esperado: CHAVE=valor;
 *   - Espacos antes/depois da chave e do valor sao descartados;
 *   - Aspas duplas ou simples envolvendo o valor sao removidas.
 *
 * Ordem de resolucao quando se chama get(chave):
 *   1) variavel de ambiente do sistema (System.getenv);
 *   2) entradas carregadas do arquivo .env;
 *   3) retorna null se nao encontrar.
 *
 * Procura o arquivo .env em (em ordem):
 *   - caminho informado em ENV_FILE (var. de ambiente);
 *   - ./.env (diretorio corrente);
 *   - ../.env (um nivel acima, util quando se executa de out/).
 */
public final class EnvLoader {

    private static final Map<String, String> entradas = new HashMap<>();
    private static boolean carregado = false;

    private EnvLoader() {
    }

    /** Carrega o .env uma unica vez. Pode ser chamado varias vezes com seguranca. */
    public static synchronized void carregar() {
        if (carregado) {
            return;
        }
        Path arquivo = localizarArquivo();
        if (arquivo != null) {
            lerArquivo(arquivo);
        }
        carregado = true;
    }

    private static Path localizarArquivo() {
        String custom = System.getenv("ENV_FILE");
        if (custom != null && !custom.isEmpty()) {
            Path p = Paths.get(custom);
            if (Files.exists(p)) {
                return p;
            }
        }
        Path p1 = Paths.get(".env");
        if (Files.exists(p1)) {
            return p1;
        }
        Path p2 = Paths.get("..", ".env");
        if (Files.exists(p2)) {
            return p2;
        }
        return null;
    }

    private static void lerArquivo(Path arquivo) {
        try (BufferedReader br = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            String linha;
            while ((linha = br.readLine()) != null) {
                processarLinha(linha);
            }
        } catch (IOException e) {
            System.err.println("Aviso: nao foi possivel ler " + arquivo + ": " + e.getMessage());
        }
    }

    private static void processarLinha(String linhaOriginal) {
        String linha = linhaOriginal.trim();
        if (linha.isEmpty() || linha.startsWith("#")) {
            return;
        }
        int igual = linha.indexOf('=');
        if (igual <= 0) {
            return;
        }
        String chave = linha.substring(0, igual).trim();
        String valor = linha.substring(igual + 1).trim();
        valor = removerAspas(valor);
        entradas.put(chave, valor);
    }

    private static String removerAspas(String valor) {
        if (valor.length() >= 2) {
            char inicio = valor.charAt(0);
            char fim = valor.charAt(valor.length() - 1);
            if ((inicio == '"' && fim == '"') || (inicio == '\'' && fim == '\'')) {
                return valor.substring(1, valor.length() - 1);
            }
        }
        return valor;
    }

    /** Retorna o valor da chave (env do sistema tem prioridade). */
    public static String get(String chave) {
        carregar();
        String doSistema = System.getenv(chave);
        if (doSistema != null && !doSistema.isEmpty()) {
            return doSistema;
        }
        return entradas.get(chave);
    }

    /** Retorna o valor da chave ou o default informado. */
    public static String get(String chave, String valorPadrao) {
        String v = get(chave);
        return (v == null || v.isEmpty()) ? valorPadrao : v;
    }
}
