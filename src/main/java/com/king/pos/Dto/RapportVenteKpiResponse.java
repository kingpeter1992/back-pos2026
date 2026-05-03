package com.king.pos.Dto;

import java.math.BigDecimal;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RapportVenteKpiResponse {

    private String cst;

    private BigDecimal totalNet;
    private BigDecimal totalPmp;
    private BigDecimal marge;

    private BigDecimal totalNetCDF;
    private BigDecimal totalPmpCDF;
    private BigDecimal margeCDF;

    private BigDecimal pourcentageMarge;
}