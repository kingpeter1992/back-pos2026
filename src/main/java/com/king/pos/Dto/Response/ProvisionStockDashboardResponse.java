package com.king.pos.Dto.Response;


import lombok.*;

import java.math.BigDecimal;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvisionStockDashboardResponse {

    private BigDecimal valeurStockTotale;
    private BigDecimal valeurStockTotaleFc;
    private BigDecimal valeurStockTotaleUsd;

    private BigDecimal provisionTotale;
    private BigDecimal provisionTotaleFc;
    private BigDecimal provisionTotaleUsd;

    private Long nombreProduits;
    private Long nombreProduitsProvisionnes;

    private List<ProvisionStockResponse> lignes;
}
