package com.king.pos.Entitys;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.king.pos.enums.StatutInventaire;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "inventaire")
public class Inventaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateInventaire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutInventaire statut;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @OneToMany(mappedBy = "inventaire", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneInventaire> lignes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (dateInventaire == null) dateInventaire = LocalDateTime.now();
        if (statut == null) statut = StatutInventaire.BROUILLON;
    }
}
