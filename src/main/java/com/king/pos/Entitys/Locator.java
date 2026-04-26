package com.king.pos.Entitys;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

@Entity
@Table(
    name = "locator",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"depot_id", "code"})
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Locator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "depot_id", nullable = false)
    private Depot depot;

    @Column(nullable = false, length = 50)
    private String code; // ex: A-01-02

    @Column(length = 150)
    private String libelle; // ex: Rayon A / Niveau 1 / Colonne 2

    @Builder.Default
    @Column(nullable = false)
    private Boolean actif = true;

    private LocalDate dateCreation;
    private LocalDate dateModification;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) dateCreation = LocalDate.now();
        if (actif == null) actif = true;
    }

    @PreUpdate
    public void preUpdate() {
        dateModification = LocalDate.now();
    }
}
