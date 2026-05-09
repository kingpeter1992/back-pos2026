package com.king.pos.Entitys;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequisitionProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Produit produit;

    private Integer totalDemandes;
    private Integer totalVentes;
    private Integer totalVentesManquees;

    private BigDecimal totalQuantiteVendue;
    private BigDecimal totalQuantiteDemandeeNonVendue;
@ManyToOne
private Depot depot;

@ManyToOne
private Locator locator;
    private Boolean produitExistant;
    private String origine;

    private LocalDateTime derniereDemande;
}
