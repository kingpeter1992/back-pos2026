package com.king.pos.Entitys;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "produit_locator",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"produit_id", "depot_id"})
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProduitLocator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "depot_id", nullable = false)
    private Depot depot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "locator_id", nullable = false)
    private Locator locator;

    @Column(nullable = false)
    private LocalDate dateAffectation;

    @Builder.Default
    @Column(nullable = false)
    private Boolean actif = true;
}