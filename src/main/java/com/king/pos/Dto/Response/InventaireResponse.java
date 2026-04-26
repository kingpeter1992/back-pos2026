package com.king.pos.Dto.Response;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.king.pos.enums.StatutInventaire;
import com.king.pos.enums.TypeInventaire;

@Data
@Builder
public class InventaireResponse {
    private Long id;
    private String reference;
    private TypeInventaire type;
    private StatutInventaire statut;
    private Long depotId;
    private String depotNom;
    private Long locatorId;
    private String locatorCode;
    private LocalDate dateInventaire;
    private LocalDateTime dateOuverture;
    private LocalDateTime dateValidation;
    private LocalDateTime dateCloture;
    private Boolean memorise;
    private Boolean gelStockTheorique;
    private Boolean varianceLancee;
    private Boolean valide;
    private Boolean cloture;
    private String commentaire;
    private Boolean bordereauxGeneres;
    private Boolean annule;
    private boolean tousBordereauxStockMisAJour;
    
}
