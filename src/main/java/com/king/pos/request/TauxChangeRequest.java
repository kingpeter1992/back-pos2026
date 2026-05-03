package com.king.pos.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TauxChangeRequest {

    @NotNull(message = "Le taux est obligatoire")
    @DecimalMin(value = "1", message = "Le taux doit être supérieur ou égal à 1")
    private BigDecimal taux;

    private Boolean actif = false;

    private String commentaire;
}