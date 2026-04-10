package com.king.pos.Entitys;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Fournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    private String telephone;
    private String email;

    private String ville;
    private String pays;
    private String description;

    @Column(columnDefinition = "TEXT")
    private String adresse;

    @Column(nullable = false)
    private Boolean actif;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @JsonIgnore
    @OneToMany(mappedBy = "fournisseur", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProduitFournisseur> produitFournisseurs = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (actif == null) actif = true;
        if (dateCreation == null) dateCreation = LocalDateTime.now();
    }
}