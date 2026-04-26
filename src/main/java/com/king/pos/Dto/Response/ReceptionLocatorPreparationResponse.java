package com.king.pos.Dto.Response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter @Getter @Builder
public class ReceptionLocatorPreparationResponse {
    private Long receptionId;
    private String refReception;
    private Long depotId;
    private String depotNom;
    private List<ReceptionLocatorPreparationLigneResponse> lignes;
}
