package com.king.pos.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ClientStatsDto {
  private long totalContrats;
  private long contratsActifs;

  private long totalFactures;
  private long facturesEmises;
  private long facturesPartial;
  private long facturesPaid;

  private double totalFactureCDF;
  private double totalFactureUSD;
  private double totalPayeCDF;
  private double totalPayeUSD;
  private double impayeCDF;
  private double impayeUSD;

  private long totalTransactions;
  private double encaissementsCDF;
  private double encaissementsUSD;
}