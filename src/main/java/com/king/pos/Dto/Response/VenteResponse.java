package com.king.pos.Dto.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenteResponse {

    private Long id;
    private String ticketNumero;
    private LocalDateTime dateVente;

    private String clientNom;
    private String caissier;
    private String modePaiement;

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
    private Long depotId;
    private String statut;

    private List<VenteLigneResponse> lignes;
}