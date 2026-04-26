package com.king.pos.Entitys;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.king.pos.enums.StatutPeremption;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stock_lot")
public class StockLot {

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
    private BigDecimal quantiteInitiale;

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal quantiteDisponible;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal prixUnitaire;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal fraisUnitaire;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal coutUnitaireFinal;

    private LocalDate dateEntree;
    private LocalDate datePeremption;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private StatutPeremption statutPeremption;

    @Column(length = 100)
    private String referenceDocument;

    @Column(length = 100)
    private String sourceDocument;

    private Long sourceDocumentId;

    private LocalDate dateCreation;
    private LocalDate dateModification;

    @Column(length = 100)
private String numeroLot;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) dateCreation = LocalDate.now();
        if (dateEntree == null) dateEntree = LocalDate.now();
        if (quantiteInitiale == null) quantiteInitiale = BigDecimal.ZERO;
        if (quantiteDisponible == null) quantiteDisponible = BigDecimal.ZERO;
        if (prixUnitaire == null) prixUnitaire = BigDecimal.ZERO;
        if (fraisUnitaire == null) fraisUnitaire = BigDecimal.ZERO;
        if (coutUnitaireFinal == null) coutUnitaireFinal = BigDecimal.ZERO;
    }

    @PreUpdate
    public void preUpdate() {
        dateModification = LocalDate.now();
    }


}
