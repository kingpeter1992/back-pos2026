package com.king.pos.Entitys;

import lombok.*;

import java.time.LocalDateTime;

import com.king.pos.enums.StatutBordereauInventaire;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventaire_bordereau")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventaireBordereau {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventaire_id")
    private Inventaire inventaire;
    
@Builder.Default
@Column(name = "variance_lancee", nullable = false)
private Boolean varianceLancee = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "depot_id")
    private Depot depot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locator_id")
    private Locator locator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutBordereauInventaire statut;

    @Column(length = 120)
    private String agentComptage;

    @Column(length = 120)
    private String validePar;

    private LocalDateTime dateSaisie;
    private LocalDateTime dateValidation;

    @Column(length = 255)
    private String commentaire;

    private Integer numeroOrdre;
    private Integer tailleBordereau;
    private Boolean afficherQuantiteTheorique;
    private Boolean stockMisAJour;
    private LocalDateTime dateMiseAJourStock;
    private LocalDateTime dateCreation;

    private String misAJourStockPar;



    @PrePersist
    public void prePersist() {
        if (dateSaisie == null) dateSaisie = LocalDateTime.now();
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (statut == null) statut = StatutBordereauInventaire.BROUILLON;
    }
}
