package com.king.pos.Entitys;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "inventaire_article",
    indexes = {
        @Index(name = "idx_inv_article_inventaire", columnList = "inventaire_id"),
        @Index(name = "idx_inv_article_produit", columnList = "produit_id"),
        @Index(name = "idx_inv_article_locator", columnList = "locator_id")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventaireLigne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventaire_id")
    private Inventaire inventaire;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id")
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "depot_id")
    private Depot depot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locator_id")
    private Locator locator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_lot_id")
    private StockLot stockLot;

    @Column(precision = 18, scale = 3, nullable = false)
    private BigDecimal stockTheorique;

    @Column(precision = 18, scale = 3)
    private BigDecimal stockPhysiqueRetenu;

    @Column(precision = 18, scale = 3)
    private BigDecimal ecartQuantite;

    @Column(precision = 18, scale = 6)
    private BigDecimal pmp;

    @Column(precision = 18, scale = 2)
    private BigDecimal valeurEcart;

    @Builder.Default
    @Column(nullable = false)
    private Boolean compte = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean varianceGeneree = false;

    @Column(length = 255)
    private String dernierCommentaire;

    private LocalDateTime derniereDateComptage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dernier_bordereau_ligne_id")
    private InventaireBordereauLigne dernierBordereauLigneValide;
}
