# FT-Coin — Sistema de Apuração de Ganhos e Perdas em Carteira de Moedas

> Projeto da disciplina **SI400 — Programação Orientada a Objetos**
> Faculdade de Tecnologia (FT/UNICAMP) — 2º semestre de 2026

Aplicação de linha de comando para administrar carteiras da moeda virtual FTC:
cadastro de carteiras, compra, venda, cotação e cinco relatórios, com apuração de
ganhos e perdas por **custo médio ponderado**.

---

## Integrantes — Grupo B3

| Nome | Papel |
|---|---|
| André Luiz Clemente de Oliveira | Product Owner / Dev |
| Arthur Luiz Lopes Araujo | Q.A. Tester / Dev |
| Beatriz Amaral | Scrum Master / Dev |
| Felipe Kenji Ouba Fukuzono | Arquiteto / Dev |
| Guilherme Gali Rocha | Analista / Dev |
| Isadora Viveiros Assumpção | Analista / Dev |
| Lorena Testa Avelar | Q.A. Tester / Dev |
| Sabrina Beatriz Fagundes Lima | Tradutora / Dev |

---

## Tecnologias

- **Java 17 LTS** — necessário para as `sealed interface` dos contratos DAO e para os `record` dos relatórios
- **Apache Maven** — build, dependências e testes
- **MariaDB** via JDBC — persistência relacional
- **JUnit 5**, **Surefire** e **JaCoCo** — testes e cobertura
- Idiomas: português, inglês e espanhol

---

## Como executar

### Com Maven (recomendado)

```bash
mvn clean package
java -jar target/ftcoin.jar
```

Para rodar sem empacotar:

```bash
mvn clean compile
java -cp "target/classes:lib/*" br.unicamp.ftcoin.Start
```

### Sem Maven, com `javac`

As dependências estão versionadas em `lib/`, então o projeto compila sem baixar nada.

**Linux / macOS**

```bash
mkdir -p out
javac --release 17 -encoding UTF-8 \
  -cp "lib/mariadb-java-client-3.4.0.jar:lib/slf4j-nop-2.0.13.jar:lib/slf4j-api-2.0.13.jar" \
  -d out $(find src/main/java -name "*.java")
java -cp "out:src/main/resources:lib/mariadb-java-client-3.4.0.jar:lib/slf4j-nop-2.0.13.jar:lib/slf4j-api-2.0.13.jar" \
  br.unicamp.ftcoin.Start
```

**Windows**

```cmd
mkdir out
dir /s /B src\main\java\*.java > sources.txt
javac --release 17 -encoding UTF-8 -cp "lib\mariadb-java-client-3.4.0.jar;lib\slf4j-nop-2.0.13.jar;lib\slf4j-api-2.0.13.jar" -d out @sources.txt
java -cp "out;src\main\resources;lib\mariadb-java-client-3.4.0.jar;lib\slf4j-nop-2.0.13.jar;lib\slf4j-api-2.0.13.jar" br.unicamp.ftcoin.Start
```

### Testes

```bash
mvn test
```

O relatório de cobertura sai em `target/site/jacoco/index.html`.

---

## Banco de dados

A persistência é escolhida na partida. **Em memória** não exige configuração e é o
modo usado nos testes e na demonstração. **MariaDB** exige um arquivo `.env` na
raiz do projeto, feito a partir de `env.example`:

```bash
cp env.example .env   # e preencha DB_PASSWORD
```

O arquivo `.env` está no `.gitignore` e nunca deve ser versionado. O servidor da
faculdade só responde dentro da intranet. A estrutura das tabelas está em
`database/POO2_2s26_B03.sql`.

---

## Arquitetura

Arquitetura em camadas com **MVC**, camada de **Service** para as regras de
negócio e **DAO** para o acesso a dados.

```
src/main/java/br/unicamp/ftcoin/
├── Start.java          Ponto de entrada: escolhe idioma e persistência e inicia o menu
├── dto/                Objetos de transferência (Wallet, Transaction, Quote, ...)
├── dao/                Contratos selados e as duas implementações, fábrica e conexão
├── oracle/             Origem das cotações (QuoteSource, FormulaQuoteSource)
├── service/            Regras de negócio e apuração por custo médio
├── view/               Telas de linha de comando, com cores
├── controller/         Orquestração e fábrica de controladores
└── util/               Console, .env, mensagens e impressão
```

```
Start → MainMenuController → Controller → Service → DAO → memória | MariaDB
                                  ↓            ↓
                                View      OracleService → QuoteSource
                                  ↓
                           MessageProvider
```

### Decisões de projeto

**Interfaces seladas (`sealed`).** `WalletDAO` e `TransactionDAO` declaram
explicitamente quais classes podem implementá-las, de modo que o compilador
garante que só existam as duas persistências previstas. Os contratos e as
implementações ficam no mesmo pacote `dao` porque, fora de um módulo nomeado, a
cláusula `permits` exige que os subtipos estejam no mesmo pacote.

**Abstract Factory.** `DAOFactory.of(PersistenceType)` devolve `MemoryDAOFactory`
ou `RelationalDAOFactory`, e é o único ponto que conhece as implementações
concretas. Como a fábrica é `AutoCloseable`, um `try`-com-recursos na partida
garante que a conexão feche na saída.

**Singleton.** `DatabaseConnection` mantém uma única conexão reaproveitada por
toda a sessão, em vez de abrir e fechar uma por operação. `MessageProvider`
também é Singleton: guarda estado com ciclo de vida — idioma, bundle e
formatadores — que precisa existir uma vez só.

**Oráculo.** A cotação de qualquer data é calculada pela fórmula do projeto
(`10,00 + flutuação + log10(25·n)`), de forma determinística: a mesma data
devolve sempre o mesmo valor. `OracleService` mantém um cache em memória, então
cada data é consultada na fonte uma única vez. `QuoteSource` **não** é selada, de
propósito: o conjunto de fontes de cotação é aberto, e uma implementação sobre
API HTTP entra sem alterar mais nada.

**Apuração.** `AverageCostCalculator` reproduz a planilha
`Docs/FTCoinApuracaoResultado.ods`. Cada compra recalcula o custo médio ponderado
pelo saldo anterior; cada venda realiza a diferença entre a cotação do dia e esse
custo médio. As divisões usam dez casas decimais e o arredondamento para centavos
acontece apenas na exibição.

---

## Internacionalização

Português, inglês e espanhol, escolhidos na partida, em arquivos `.properties`
lidos como UTF-8. Além dos textos, números, datas e moeda seguem o idioma: o
símbolo e os separadores mudam, mas a unidade continua sendo o real, porque a
FT-Coin é cotada em reais independentemente de quem lê a tela.

| Idioma | Valor | Data |
|---|---|---|
| pt-BR | `R$ 12.287,94` | `01/01/2026` |
| en-US | `R$12,287.94` | `1/1/26` |
| es-ES | `12.287,94 BRL` | `1/1/26` |

---

## Testes

52 testes cobrindo serviços, oráculo, DTOs e persistência em memória. O principal
é `AverageCostCalculatorTest.reproducesReferenceSpreadsheet`, que replica os 30
dias da planilha de apuração e confere saldo, custo médio, total comprado, total
vendido, resultado acumulado e as duas valorações do saldo.

Telas, controladores e DAO relacional ficam fora da medição de cobertura: os dois
primeiros são laços sobre entrada e saída padrão, e o terceiro depende do MariaDB
da faculdade, que só responde pela intranet.
