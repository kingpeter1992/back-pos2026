package com.king.pos.Entitys;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter @Getter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "produit")
public class Produit {
 @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_barres", nullable = false, unique = true, length = 100)
    private String codeBarres;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @Column(name = "prix_achat", precision = 12, scale = 2)
    private BigDecimal prixAchat;

    private BigDecimal prixVenteFc;
    private BigDecimal prixVenteUsd;
    private BigDecimal tauxChangeUtilise;

    @Column(name = "prix_vente", precision = 12, scale = 2, nullable = false)
    private BigDecimal prixVente;

    @Column(name = "stock_minimum", nullable = false)
    private BigDecimal stockMinimum;

    @Column(name = "stock_maximum", nullable = false)
    private BigDecimal stockMaximum;

    @Column(nullable = false)
    private Boolean actif;

    @Column(length = 20, nullable = false)
    private String perissable;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ImagePhoto> images = new ArrayList<>();

    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProduitFournisseur> produitFournisseurs = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (actif == null) {
            actif = true;
        }
        if (stockMinimum == null) {
            stockMinimum = BigDecimal.ZERO;
        }
        if (stockMaximum == null) {
            stockMaximum = BigDecimal.ZERO;
        }
        if (perissable == null || perissable.isBlank()) {
            perissable = "NON";
        }
    }

    public boolean isPerissable() {
        return "OUI".equalsIgnoreCase(this.perissable);
    }
}