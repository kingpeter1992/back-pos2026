package com.king.pos.Entitys;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "vente")
public class Vente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateVente;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModePaiement modePaiement;
    
    private BigDecimal totalHT; //TotalHT
    private BigDecimal totalRemise; //TotalRemise
    private BigDecimal totalTTC; //TotalTTC

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