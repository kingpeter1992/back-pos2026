package com.king.pos.Entitys;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.king.pos.enums.TypeMouvementStock;

@Entity
@Table(name = "transaction_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateTransaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TypeMouvementStock typeMouvement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id")
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "depot_id")
    private Depot depot;

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal quantite;

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal stockAvant;

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal stockApres;

    @Column(precision = 18, scale = 6)
    private BigDecimal pmpAvant;

    @Column(precision = 18, scale = 6)
    private BigDecimal pmpApres;

    @Column(precision = 18, scale = 6)
    private BigDecimal prixUnitaire;

    @Column(precision = 18, scale = 6)
    private BigDecimal fraisUnitaire;

    @Column(precision = 18, scale = 6)
    private BigDecimal coutUnitaireFinal;

    @Column(length = 100)
    private String referenceDocument;

    @Column(length = 50)
    private String sourceDocument; // VENTE, RECEPTION, INVENTAIRE...

    private Long sourceDocumentId;

    @Column(length = 255)
    private String libelle;

    @Column(length = 100)
    private String utilisateur;

    @Column(precision = 18, scale = 6)
    private BigDecimal tauxChangeUtilise;
    


}