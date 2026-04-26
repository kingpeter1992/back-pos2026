package com.king.pos.Entitys;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.king.pos.enums.StatutInventaire;
import com.king.pos.enums.TypeInventaire;

import java.time.LocalDate;

@Entity
@Table(name = "inventaire")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Inventaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeInventaire type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private StatutInventaire statut;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "depot_id")
    private Depot depot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locator_id")
    private Locator locator;

    private LocalDate dateInventaire;
    private LocalDateTime dateOuverture;
    private LocalDateTime dateValidation;
    private LocalDateTime dateCloture;

    private Boolean bordereauxGeneres;
    private Boolean annule;
    private String annulePar;
    private LocalDateTime dateAnnulation;
    private String commentaireAnnulation;

    @Column(length = 120)
    private String creePar;

    @Column(length = 120)
    private String validePar;

    @Column(length = 120)
    private String cloturePar;

    @Column(length = 255)
    private String commentaire;

    @Builder.Default
    @Column(nullable = false)
    private Boolean memorise = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean gelStockTheorique = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean varianceLancee = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean valide = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean cloture = false;

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (statut == null) statut = StatutInventaire.BROUILLON;
        if (memorise == null) memorise = true;
        if (gelStockTheorique == null) gelStockTheorique = true;
        if (varianceLancee == null) varianceLancee = false;
        if (valide == null) valide = false;
        if (cloture == null) cloture = false;
    }

    @PreUpdate
    public void preUpdate() {
        dateModification = LocalDateTime.now();
    }
}
