package com.king.pos.Entitys;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "vente_lot_consommation")
public class VenteLotConsommation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vente_id")
    private Vente vente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ligne_vente_id")
    private LigneVente ligneVente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_lot_id")
    private StockLot stockLot;

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal quantiteConsommee;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal coutUnitaireAuMomentVente;

    private LocalDate datePeremptionAuMomentVente;

    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (quantiteConsommee == null) {
            quantiteConsommee = BigDecimal.ZERO;
        }
        if (coutUnitaireAuMomentVente == null) {
            coutUnitaireAuMomentVente = BigDecimal.ZERO;
        }
    }
}