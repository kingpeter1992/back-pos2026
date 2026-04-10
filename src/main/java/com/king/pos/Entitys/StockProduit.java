package com.king.pos.Entitys;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


import java.math.BigDecimal;

@Entity
@Getter
@Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"produit_id", "depot_id"})
})
public class StockProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id")
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "depot_id")
    private Depot depot;

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal quantiteDisponible;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal pmp;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal valeurStock;

    private LocalDateTime dateDerniereEntree;
    private LocalDateTime dateDerniereSortie;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    @PrePersist
    public void prePersist() {
        if (quantiteDisponible == null) quantiteDisponible = BigDecimal.ZERO;
        if (pmp == null) pmp = BigDecimal.ZERO;
        if (valeurStock == null) valeurStock = BigDecimal.ZERO;
        if (dateCreation == null) dateCreation = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        dateModification = LocalDateTime.now();
    }
}