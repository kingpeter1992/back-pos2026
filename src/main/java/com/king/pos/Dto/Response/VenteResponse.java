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
    private BigDecimal montantRecu;
    private BigDecimal monnaie;
    private BigDecimal sousTotal;
    private BigDecimal totalRemise;
    private BigDecimal totalGeneral;
    private String devise;
    private Long tarifId;
    private List<VenteLigneResponse> lignes;
}