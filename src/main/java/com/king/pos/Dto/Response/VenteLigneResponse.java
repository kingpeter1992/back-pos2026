package com.king.pos.Dto.Response;

import java.math.BigDecimal;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenteLigneResponse {
    private Long produitId;
    private String produitNom;
    private BigDecimal quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal remise;
    private BigDecimal totalLigne;
}