package com.king.pos.Entitys;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.king.pos.enums.Devise;
import com.king.pos.enums.StatutCommandeFournisseur;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "commande_achat")
public class CommandeAchat {

     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String refCommande;

    private LocalDate dateCommande;
    private LocalDate dateLivraisonPrevue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    private String prefixe;

    @Enumerated(EnumType.STRING)
    private Devise devise;

    @Column(precision = 18, scale = 6)
    private BigDecimal taux;

    @Enumerated(EnumType.STRING)
    private StatutCommandeFournisseur statut;

    @Column(precision = 18, scale = 2)
    private BigDecimal montantBrut;
     @Column(precision = 18, scale = 2)
    private BigDecimal montantRemise;

     @Column(precision = 18, scale = 2)
    private BigDecimal montantTotal;


    @Column(columnDefinition = "TEXT")
    private String observation;

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    @OneToMany(mappedBy = "commandeAchat", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<CommandeAchatLigne> lignes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (dateCommande == null) dateCommande = LocalDate.now();
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (statut == null) statut = StatutCommandeFournisseur.BROUILLON;
        if (montantTotal == null) montantTotal = BigDecimal.ZERO;
        if (taux == null) taux = BigDecimal.ONE;
        if (devise == null) devise = Devise.USD;
        if (montantBrut == null) montantBrut = BigDecimal.ZERO;
        if (montantRemise == null) montantRemise = BigDecimal.ZERO;
    }




    @PreUpdate
    public void preUpdate() {
        dateModification = LocalDateTime.now();
    }

}