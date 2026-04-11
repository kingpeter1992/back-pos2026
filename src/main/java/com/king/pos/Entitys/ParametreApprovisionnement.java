package com.king.pos.Entitys;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "parametre_approvisionnement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametreApprovisionnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delai_approvisionnement_jours", nullable = false)
    private Integer delaiApprovisionnementJours = 5;

    @Column(name = "jours_securite", nullable = false)
    private Integer joursSecurite = 3;

    @Column(name = "couverture_cible_jours", nullable = false)
    private Integer couvertureCibleJours = 15;

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;
}