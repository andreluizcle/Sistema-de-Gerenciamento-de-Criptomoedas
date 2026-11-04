package br.unicamp.ftcoin.dao;

/** Modos de persistencia oferecidos na partida do programa. */
public enum PersistenceType {

    /** Simulacao em memoria, sobre colecoes Java. */
    MEMORY,

    /** Banco relacional MariaDB, acessado por JDBC. */
    RELATIONAL
}
