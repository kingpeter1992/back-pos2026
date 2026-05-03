package com.king.pos.Entitys;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "taux_change")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TauxChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal taux;

    @Column(nullable = false)
    private Boolean actif = false;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateActivation;

    @Column(length = 255)
    private String commentaire;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();

        if (this.actif == null) {
            this.actif = false;
        }

        if (Boolean.TRUE.equals(this.actif)) {
            this.dateActivation = LocalDateTime.now();
        }
    }
}