/*
 * ************************************************************************************************************************
 * Prof. Dr. André F. de Angelis
 * Unicamp - Universidade Estadual de Campinas
 * FT - Faculdade de Tecnologia
 * ************************************************************************************************************************
 * Escrito em Java 20, usando IDE Eclipse 2023-06, em ambiente Windows 11
 *
 * Versão 1.0
 * 07.Outubro.2026
 *
 * ************************************************************************************************************************
 *
 * Este classe é o DTO de um sistema cliente/servidor para demonstração do uso de fluxos (streams) de objetos
 * enviados e recebidos pela rede, com uso de uma API proprietária para acesso ao servidor.
 *
 * Esta classe carrega dados da cotação de uma moeda (FTCoin) entre o servidor e o cliente (Data Transfer Object - DTO)
 *
 * Nota do Grupo B3: arquivo reconvertido de ISO-8859-1 para UTF-8, sem
 * qualquer alteracao de codigo, porque o projeto compila com -encoding UTF-8.
 *
 * O pacote permanece "network", fora de br.unicamp.ftcoin, porque a
 * desserializacao do Java resolve a classe pelo nome completo gravado no
 * fluxo: mover de pacote quebraria a comunicacao com o servidor.
 *
 * Projeto FTCoin, ver. 2026.
 *
 * ************************************************************************************************************************
 */

package network;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class ExchangeRate implements Serializable
   {
   private static final long serialVersionUID = 1L;

   LocalDate                 date;
   BigDecimal                value;

   public ExchangeRate(LocalDate date, BigDecimal value)
      {
      super();
      this.date  = date;
      this.value = value;
      }

   public final LocalDate getDate()
      {
      return date;
      }

   public final BigDecimal getValue()
      {
      return value;
      }

   }
