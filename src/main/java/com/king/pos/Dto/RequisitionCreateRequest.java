package com.king.pos.Dto;


import java.math.BigDecimal;


import lombok.Data;

@Data
public class RequisitionCreateRequest {

    private Long produitId;

    private Long depotId;

    private BigDecimal quantite;

    private TypeRequisition type;

    private Boolean produitExistant;

    private String origine;

    private String commentaire;

    private String creePar;
}