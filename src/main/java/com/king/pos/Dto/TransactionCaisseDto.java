package com.king.pos.Dto;

import java.time.LocalDateTime;

import com.king.pos.Entitys.Categorie;
import com.king.pos.Entitys.ModePaiement;
import com.king.pos.Entitys.TypeTransaction;
import com.king.pos.enums.Devise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TransactionCaisseDto {

 public TransactionCaisseDto(Long id2, Devise devise2, LocalDateTime dateTransaction2, TypeTransaction type2,
            Categorie category2, ModePaiement modePaiement2, double montant2, String sens2,
            String description2, String reference2, double soldeAvant2, double soldeApres2, String userId2) {
        //TODO Auto-generated constructor stub
        this.id = id2;
        this.devise = devise2;
        this.dateTransaction = dateTransaction2;
        this.type = type2;
        this.category = category2;
        this.modePaiement = modePaiement2;
        this.montant = montant2;
        this.sens = sens2;
        this.description = description2;
        this.reference = reference2;
        this.soldeAvant = soldeAvant2;
        this.soldeApres = soldeApres2;
        this.userId = userId2;
    }
 private Long id;
    private LocalDateTime dateTransaction;

    private TypeTransaction type;
    private Categorie category;
    private Devise devise;
    private ModePaiement modePaiement;

    private double montant;
    private String sens;

    private String description;
    private String reference;

    private double soldeAvant;
    private double soldeApres;

    private String userId;

    private Long clientId;
    private String nomClient;

    private Long gardienId;
    private String nomGardien;


  
    
}
