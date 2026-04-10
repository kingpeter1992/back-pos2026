package com.king.pos.Dto;

import lombok.*;
import java.time.LocalDate;


@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RetenueDTO {
    private Long id;
    private Long employeId;
    private String employeNom;

    private String libelle;
    private double montant;
    private LocalDate dateRetenue;
    private String motif;
}