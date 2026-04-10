package com.king.pos.Entitys;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(
    name = "produit_fournisseur",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_pf_unique", columnNames = {"produit_id", "fournisseur_id"})
    }
)
public class ProduitFournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fournisseur_id", nullable = false)
    private Fournisseur fournisseur;

    @Column(name = "reference_fournisseur", length = 100)
    private String referenceFournisseur;

    @Column(name = "prix_achat", precision = 12, scale = 2)
    private BigDecimal prixAchat;

    @Column(name = "delai_livraison_jours")
    private Integer delaiLivraisonJours;

    @Column(name = "quantite_min_commande")
    private Integer quantiteMinCommande;

    @Column(name = "fournisseur_principal", nullable = false)
    private Boolean fournisseurPrincipal;

    @Column(nullable = false)
    private Boolean actif;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        if (delaiLivraisonJours == null) delaiLivraisonJours = 3;
        if (quantiteMinCommande == null) quantiteMinCommande = 1;
        if (fournisseurPrincipal == null) fournisseurPrincipal = false;
        if (actif == null) actif = true;
        if (dateCreation == null) dateCreation = LocalDateTime.now();
    }
}