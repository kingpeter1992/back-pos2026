package com.king.pos.Entitys;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventaire_bordereau_ligne", indexes = {
        @Index(name = "idx_bord_ligne_bordereau", columnList = "bordereau_id"),
        @Index(name = "idx_bord_ligne_article", columnList = "inventaire_article_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventaireBordereauLigne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bordereau_id")
    private InventaireBordereau bordereau;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventaire_article_id")
    private InventaireLigne inventaireArticle;

    @Column(precision = 18, scale = 3)
    private BigDecimal quantiteComptee;

    @Builder.Default
    @Column(nullable = false)
    private Boolean retenue = false;

    @Column(length = 255)
    private String commentaire;

    @Column(length = 100)
    private String saisiPar;

    private LocalDateTime dateSaisie;
    private LocalDateTime dateValidation;
    private Integer numeroLigne;

@Builder.Default
@Column(name = "variance_generee", nullable = false)
private Boolean varianceGeneree = false;
    @PrePersist
    public void prePersist() {
        if (dateSaisie == null)
            dateSaisie = LocalDateTime.now();
        if (retenue == null)
            retenue = false;
    }
}
