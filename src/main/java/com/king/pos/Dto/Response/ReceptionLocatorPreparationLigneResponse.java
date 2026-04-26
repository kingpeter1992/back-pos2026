package com.king.pos.Dto.Response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class ReceptionLocatorPreparationLigneResponse {
    private Long produitId;
    private String produitNom;
    private BigDecimal quantiteRecue;
    private Long locatorId;
    private String locatorCode; // dernier locator trouvé
}
