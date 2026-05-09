package com.king.pos.Entitys;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.king.pos.Dto.TypeRequisition;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Entity
@Data
@Builder
public class RequisitionHistorique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Produit produit;

    private LocalDate date;
    private LocalDateTime dateHeure;

    @Enumerated(EnumType.STRING)
    private TypeRequisition type;

    private BigDecimal quantiteDemandee;
    private BigDecimal quantiteVendue;

    private Integer nombreDemandes;
    private Integer nombreVentes;
    private Integer nombreVentesManquees;

    private Boolean produitExistant;
    private String origine;
    @ManyToOne
    private Depot depot;

    @ManyToOne
    private Locator locator;
    private String creePar;
}