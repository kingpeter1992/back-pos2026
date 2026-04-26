package com.king.pos.Entitys;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tarif_vente_id", "categorie_id"})
})

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TarifCategorieProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private TarifVente tarifVente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Categorie categorie;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tauxMarge; // ex: 20 = 20%

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tauxRemiseMax; // ex: 5 = 5%

    @Column(nullable = false)
    private Boolean actif = true;

    @Column(precision = 10, scale = 2)
    private BigDecimal prixMinimum; // facultatif

    @Column(precision = 10, scale = 2)
    private BigDecimal prixMaximum; // facultatif

    @Column(length = 20)
    private String modeArrondi; // ENTIER_SUP, MULTIPLE_100, AUCUN
    @Column
    private LocalDateTime dateCreation;
      @PrePersist
    public void prePersist() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
    }
}