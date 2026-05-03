package com.king.pos.Entitys;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.king.pos.enums.StatutVente;


@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "vente")
public class Vente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateVente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id")
    private Depot depot;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModePaiement modePaiement;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vente_origine_id")
    private Vente venteOrigine;

    @Column(name = "commentaire_annulation", length = 500)
    private String commentaireAnnulation;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutVente statut;
    private BigDecimal taux;
    private BigDecimal totalHT; //TotalHT
    private BigDecimal totalRemise; //TotalRemise
    private BigDecimal totalTva; //TotalTVA
    private BigDecimal totalTTC; //TotalTTC


    @Column(precision = 19, scale = 6)
private BigDecimal tauxChange;

@Column(precision = 19, scale = 2)
private BigDecimal sousTotalCDF;

@Column(precision = 19, scale = 2)
private BigDecimal totalRemiseCDF;

@Column(precision = 19, scale = 2)
private BigDecimal totalGeneralCDF;

@Column(precision = 19, scale = 2)
private BigDecimal montantRecuCDF;

@Column(precision = 19, scale = 2)
private BigDecimal monnaieCDF;

@Column(precision = 19, scale = 2)
private BigDecimal sousTotalUSD;

@Column(precision = 19, scale = 2)
private BigDecimal totalRemiseUSD;

@Column(precision = 19, scale = 2)
private BigDecimal totalGeneralUSD;

@Column(precision = 19, scale = 2)
private BigDecimal montantRecuUSD;

@Column(precision = 19, scale = 2)
private BigDecimal monnaieUSD;

    private String devise;

    @Column(length = 100)
    private String caissier;

    private String ticketNumero;
    private String clientNom;
    private BigDecimal montantRecu;
    private BigDecimal monnaie;

    @OneToMany(mappedBy = "vente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneVente> lignes = new ArrayList<>();



    @PrePersist
    public void prePersist() {
        if (dateVente == null) dateVente = LocalDateTime.now();
        if (total == null) total = BigDecimal.ZERO;
    }
}