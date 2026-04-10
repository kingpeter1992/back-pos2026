package com.king.pos.Entitys;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Caisse {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double montantInitialUSD;
    private double montantActuelUSD;

    private double montantInitialCDF;
    private double montantActuelCDF;

    private LocalDateTime date;

    private String description;

    // @OneToMany(mappedBy = "caisse")
    // private List<TransactionCaisse> transactions;

}
