package com.king.pos.Entitys;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tarif_vente_id", "produit_id"})
})
public class TarifProduitSpecifique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private TarifVente tarifVente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Produit produit;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tauxMarge;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tauxRemiseMax;

    @Column(nullable = false)
    private Boolean actif = true;
}
