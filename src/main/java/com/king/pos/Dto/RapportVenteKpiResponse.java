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


private BigDecimal totalNetUSD;
private BigDecimal totalPmpUSD;
private BigDecimal margeUSD;
private BigDecimal prixBrutCDF;
private BigDecimal prixBrutUSD;
private BigDecimal prixNetCDF;
private BigDecimal prixNetUSD;
private BigDecimal remiseCDF;
private BigDecimal remiseUSD;
private BigDecimal pmpCDF;
private BigDecimal pmpUSD;
private BigDecimal totalTtcCDF;
private BigDecimal totalTtcUSD;
    
}