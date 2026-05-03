package com.king.pos.Entitys;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.king.pos.enums.Devise;
import com.king.pos.enums.StatutReceptionAchat;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReceptionAchat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String refReception;

    private LocalDate dateReception;

    @Column(length = 100)
    private String numeroLivraison;

    @Column(length = 100)
    private String numeroFactureFournisseur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_achat_id")
    private CommandeAchat commandeAchat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id")
    private Depot depot;

    @Enumerated(EnumType.STRING)
    private Devise devise;

    @Column(precision = 18, scale = 6)
    private BigDecimal taux;

    @Enumerated(EnumType.STRING)
    private StatutReceptionAchat statut;

    @Column(precision = 18, scale = 2)
    private BigDecimal fraisTransport;

    @Column(precision = 18, scale = 2)
    private BigDecimal fraisDouane;

    @Column(precision = 18, scale = 2)
    private BigDecimal fraisManutention;

    @Column(precision = 18, scale = 2)
    private BigDecimal autresFrais;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalMarchandise;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalFrais;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalGeneral;

    @Column(columnDefinition = "TEXT")
    private String observation;

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    @Column(precision = 18, scale = 6)
private BigDecimal tauxChangeUtilise;

@Column(precision = 18, scale = 2)
private BigDecimal montantMarchandiseFc;

@Column(precision = 18, scale = 2)
private BigDecimal montantMarchandiseUsd;

@Column(precision = 18, scale = 2)
private BigDecimal montantFraisFc;

@Column(precision = 18, scale = 2)
private BigDecimal montantFraisUsd;

@Column(precision = 18, scale = 2)
private BigDecimal montantTotalFc;

@Column(precision = 18, scale = 2)
private BigDecimal montantTotalUsd;

@Column(length = 100)
private String observateur;

    @OneToMany(mappedBy = "receptionAchat", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReceptionAchatLigne> lignes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (dateReception == null) dateReception = LocalDate.now();
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (statut == null) statut = StatutReceptionAchat.BROUILLON;
        if (devise == null) devise = Devise.USD;
        if (taux == null) taux = BigDecimal.ONE;

        if (fraisTransport == null) fraisTransport = BigDecimal.ZERO;
        if (fraisDouane == null) fraisDouane = BigDecimal.ZERO;
        if (fraisManutention == null) fraisManutention = BigDecimal.ZERO;
        if (autresFrais == null) autresFrais = BigDecimal.ZERO;

        if (totalMarchandise == null) totalMarchandise = BigDecimal.ZERO;
        if (totalFrais == null) totalFrais = BigDecimal.ZERO;
        if (totalGeneral == null) totalGeneral = BigDecimal.ZERO;
    }

    @PreUpdate
    public void preUpdate() {
        dateModification = LocalDateTime.now();
        if (devise == null) devise = Devise.CDF;
if (taux == null) taux = BigDecimal.ZERO;
if (tauxChangeUtilise == null) tauxChangeUtilise = taux;

if (montantMarchandiseFc == null) montantMarchandiseFc = BigDecimal.ZERO;
if (montantMarchandiseUsd == null) montantMarchandiseUsd = BigDecimal.ZERO;
if (montantFraisFc == null) montantFraisFc = BigDecimal.ZERO;
if (montantFraisUsd == null) montantFraisUsd = BigDecimal.ZERO;
if (montantTotalFc == null) montantTotalFc = BigDecimal.ZERO;
if (montantTotalUsd == null) montantTotalUsd = BigDecimal.ZERO;
    }

    public BigDecimal calculTotalFrais() {
        return nvl(fraisTransport)
                .add(nvl(fraisDouane))
                .add(nvl(fraisManutention))
                .add(nvl(autresFrais));
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }  
}
