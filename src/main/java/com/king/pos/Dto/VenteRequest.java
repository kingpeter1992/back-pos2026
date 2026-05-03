package com.king.pos.Dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

import com.king.pos.Entitys.ModePaiement;



@Getter
@Setter
public class VenteRequest {

    private String ticketNumero;
    private String clientNom;
    private String caissier;

    private Long depotId;
    private ModePaiement modePaiement;

    private String devise;
    private BigDecimal tauxChange;

    private BigDecimal montantRecu;
    private BigDecimal monnaie;
    private BigDecimal sousTotal;
    private BigDecimal totalRemise;
    private BigDecimal totalGeneral;

    private BigDecimal sousTotalCDF;
    private BigDecimal totalRemiseCDF;
    private BigDecimal totalGeneralCDF;
    private BigDecimal montantRecuCDF;
    private BigDecimal monnaieCDF;

    private BigDecimal sousTotalUSD;
    private BigDecimal totalRemiseUSD;
    private BigDecimal totalGeneralUSD;
    private BigDecimal montantRecuUSD;
    private BigDecimal monnaieUSD;

    private Long tarifId;

    private List<LigneVenteRequest> lignes;
}