# 💼 Sistema de Carteira de Criptomoedas

> Projeto desenvolvido para a disciplina de **Programação Orientada a Objetos** — Faculdade de Tecnologia (UNICAMP)

---

## 👥 Integrantes — Grupo B3

| Nome | Papel |
|---|---|
| André Luiz Clemente de Oliveira | Product Owner (P.O.) / Dev |
| Arthur Luiz Lopes Araujo | Q.A. Tester / Dev |
| Beatriz Amaral | Scrum Master / Dev |
| Felipe Kenji Ouba Fukuzono | Arquiteto / Dev |
| Guilherme Gali Rocha | Analista / Dev |
| Isadora Viveiros Assumpção | Analista / Dev |
| Lorena Testa Avelar | Q.A. Tester / Dev |
| Sabrina Beatriz Fagundes Lima | Tradutora / Dev |

---

## 📋 Sobre o Projeto

Sistema de gerenciamento de carteira de criptomoedas desenvolvido em Java, com interface via linha de comando (CLI). O sistema permite realizar movimentações (compra e venda de moedas), gerenciar carteiras, consultar cotações via o módulo **Oráculo** e gerar relatórios.

---

## 🛠️ Tecnologias

- **Linguagem:** Java
- **Banco de dados:** MariaDB
- **Metodologia:** Kanban (gerenciado via Notion)
- **Versionamento:** Git / GitHub
- **Comunicação:** WhatsApp + Google Meet

---

## 🏗️ Arquitetura

O sistema adota a **Arquitetura em Camadas** com o padrão **MVC (Model-View-Controller)**, acrescida de uma camada **Service** para encapsular as regras de negócio, e objetos **DAO** para interação com os bancos de dados.

```
src/
└── java/
    ├── dto/                    # Objetos de transferência de dados
    │   ├── Carteira.java
    │   ├── Movimentacao.java
    │   └── Oraculo.java
    ├── view/                   # Interface CLI (E/S)
    │   ├── MenuPrincipalView.java
    │   ├── CarteiraView.java
    │   ├── MovimentacaoView.java
    │   └── RelatorioView.java
    ├── controller/             # Orquestração das chamadas
    │   ├── MenuPrincipalController.java
    │   ├── CarteiraController.java
    │   ├── MovimentacaoController.java
    │   └── RelatorioController.java
    ├── service/                # Regras de negócio
    │   ├── CarteiraService.java
    │   ├── MovimentacaoService.java
    │   └── OraculoService.java
    ├── dao/
    │   ├── interface/          # Contratos de acesso ao banco
    │   │   ├── ICarteiraDAO.java
    │   │   ├── IMovimentacaoDAO.java
    │   │   └── IOraculoDAO.java
    │   └── relational/         # Banco de dados relacional (MariaDB)
    │       ├── CarteiraRelationalDAO.java
    │       ├── MovimentacaoRelationalDAO.java
    │       └── OraculoRelationalDAO.java
    ├── MessageProvider.java    # Internacionalização / tradução
    └── Start.java              # Ponto de entrada da aplicação
└── main/
    └── resources/
        ├── message_en_US.properties
        └── message_pt_BR.properties
```

---

## 🔄 Fluxo da Arquitetura

```
Start → MenuPrincipalController → Controller → Service → DAO (MariaDB)
                                       ↕
                                      View
                                       ↕
                                 MessageProvider
```

---

## ✅ Progresso por Sprint

### Sprint 1 — Planejamento (semanas 1–3)
- Definição da arquitetura e metodologia
- Divisão de tarefas e papéis
- Criação do cronograma
- Diagrama de classes
- Entrega do Sumário Executivo I

### Sprint 2 — Implementação base (semanas 4–6)
- Implementação das classes relacionadas à **Carteira**:
  `CarteiraService`, `Carteira`, `CarteiraView`, `CarteiraRelationalDAO`, `CarteiraController`, `ICarteiraDAO`
- `MessageProvider.java` e `message_pt_BR.properties`
- Revisão da arquitetura com base no feedback do professor
- Entrega do Sumário Executivo II

### Sprint 3 — Em progresso (semanas 7–8)
- Implementação das classes relacionadas à **Movimentação**:
  `MovimentacaoService`, `Movimentacao`, `MovimentacaoView`, `MovimentacaoRelationalDAO`, `MovimentacaoController`, `IMovimentacaoDAO`

### Sprints 4+ — Planejados
- Implementação do Oráculo e relatórios
- Ajustes de arquitetura
- Correção de erros
- Finalização da documentação

---

## 🌍 Internacionalização

O sistema suporta múltiplos idiomas via arquivos `.properties`:
- 🇧🇷 Português do Brasil: `message_pt_BR.properties`
- 🇺🇸 Inglês: `message_en_US.properties`

---

## ⚙️ Como Executar

> **Pré-requisitos:** Java 11+ e MariaDB instalados e configurados.

### 1. Clone o repositório

```bash
git clone https://github.com/<seu-usuario>/<seu-repositorio>.git
cd ftcoin
```

### 2. Estrutura esperada

O repositório já inclui a pasta `lib/` com as dependências necessárias:

```
ftcoin/
├── src/
├── lib/
│   ├── mariadb-java-client-3.4.0.jar
│   ├── slf4j-nop-2.0.13.jar
│   └── slf4j-api-2.0.13.jar
└── README.md
```

### 3. Compile o projeto

**Windows:**
```cmd
mkdir out
dir /s /B src\main\java\*.java > sources.txt
javac -cp "lib/mariadb-java-client-3.4.0.jar;lib/slf4j-nop-2.0.13.jar;lib/slf4j-api-2.0.13.jar" -d out @sources.txt
```

**Linux / macOS:**
```bash
mkdir -p out
javac -cp "lib/mariadb-java-client-3.4.0.jar:lib/slf4j-nop-2.0.13.jar:lib/slf4j-api-2.0.13.jar" -d out $(find src -name "*.java")
```

### 4. Execute

**Windows:**
```cmd
java -cp "out;src/main/resources;lib/mariadb-java-client-3.4.0.jar;lib/slf4j-nop-2.0.13.jar;lib/slf4j-api-2.0.13.jar" br.unicamp.ftcoin.Start
```

**Linux / macOS:**
```bash
java -cp "out:src/main/resources:lib/mariadb-java-client-3.4.0.jar:lib/slf4j-nop-2.0.13.jar:lib/slf4j-api-2.0.13.jar" br.unicamp.ftcoin.Start
```

---

## 📌 Observações Arquiteturais

- A camada **Service** foi adicionada para desacoplar as regras de negócio dos Controllers. Os Controllers atuam como orquestradores, enquanto os Services interagem com a camada DAO.
- A classe `RelatorioService` foi **removida** após revisão, pois suas funcionalidades se sobrepunham às demais classes de serviço.
