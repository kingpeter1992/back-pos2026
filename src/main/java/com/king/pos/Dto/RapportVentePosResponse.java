package com.king.pos.Dto;

import java.util.List;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RapportVentePosResponse {

    private List<RapportVenteKpiResponse> kpis;

    private List<RapportVenteDetailResponse> details;

    private RapportVenteKpiResponse totalGeneral;
}