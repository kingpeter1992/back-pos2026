package com.king.pos.Entitys;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.king.pos.enums.TypeVariance;

@Entity
@Table(name = "inventaire_variance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventaireVariance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventaire_id")
    private Inventaire inventaire;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventaire_article_id")
    private InventaireLigne inventaireArticle;

    @Column(precision = 18, scale = 3, nullable = false)
    private BigDecimal stockTheorique;

    @Column(precision = 18, scale = 3, nullable = false)
    private BigDecimal stockPhysiqueRetenu;

    @Column(precision = 18, scale = 3, nullable = false)
    private BigDecimal ecart;

    @Column(precision = 18, scale = 6)
    private BigDecimal pmp;

    @Column(precision = 18, scale = 2)
    private BigDecimal valeurEcart;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeVariance type;

    @Builder.Default
    @Column(nullable = false)
    private Boolean appliquee = false;

    private LocalDateTime dateApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bordereau_id")
    private InventaireBordereau bordereau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ligne_bordereau_id")
    private InventaireBordereauLigne ligneBordereau;

    @PrePersist
    public void prePersist() {
        if (dateApplication == null)
            dateApplication = LocalDateTime.now();
    }
}
