package com.king.pos.Dto;

import java.util.List;

import com.king.pos.Dto.Response.InventaireResponse;

import lombok.*;




@Data
@Builder
public class InventaireResultatResponse {

    private InventaireResponse inventaire;

    private InventaireResultatKpiResponse kpi;

    private List<InventaireResultatLigneResponse> lignes;
}