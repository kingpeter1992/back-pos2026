package com.king.pos.Entitys;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "ligne_achat_ligne")
public class CommandeAchatLigne {

   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_achat_id", nullable = false)
    @JsonBackReference
    private CommandeAchat commandeAchat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(precision = 15, scale = 2)
private BigDecimal remise;

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal quantiteCommandee;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(precision = 18, scale = 3)
    private BigDecimal quantiteRecue;

    @Column(precision = 18, scale = 2)
    private BigDecimal montantLigne;

    @PrePersist
    @PreUpdate
    public void preSave() {
        if (quantiteRecue == null) quantiteRecue = BigDecimal.ZERO;
        if (quantiteCommandee == null) quantiteCommandee = BigDecimal.ZERO;
        if (prixUnitaire == null) prixUnitaire = BigDecimal.ZERO;
        if (remise == null) remise = BigDecimal.ZERO;
        montantLigne = quantiteCommandee.multiply(prixUnitaire);
    }
}