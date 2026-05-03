package com.king.pos.request;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TauxChangeResponse {

    private Long id;
    private BigDecimal taux;
    private Boolean actif;
    private LocalDateTime dateCreation;
    private LocalDateTime dateActivation;
    private String commentaire;
}
