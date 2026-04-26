package com.king.pos.Entitys;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reception_locator_ligne")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReceptionLocatorLigne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reception_id", nullable = false)
    private ReceptionAchat reception;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "locator_id", nullable = false)
    private Locator locator;

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal quantiteRangee;

    private LocalDate dateCreation;
}