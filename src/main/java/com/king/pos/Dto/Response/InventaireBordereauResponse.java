package com.king.pos.Dto.Response;


import com.king.pos.enums.StatutBordereauInventaire;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InventaireBordereauResponse {
    private Long id;
    private String reference;
    private Long inventaireId;
    private String depotNom;
    private Integer tailleBordereau;
    private Integer numeroOrdre;
    private Integer nombreLignes;
    private Boolean afficherQuantiteTheorique;
    private Boolean stockMisAJour;
    private StatutBordereauInventaire statut;
    private String agentComptage;
    private String validePar;
    private LocalDateTime dateSaisie;
    private LocalDateTime dateValidation;
    private LocalDateTime dateMiseAJourStock;
    private LocalDateTime dateCreation;
    private String commentaire;
    private Long locatorId;
    private String locatorCode;


    

}