package com.king.pos.Dto;

public record CaisseSummaryDTO(
        double totalEncUSD,
        double totalDecUSD,
        double totalEncCDF,
        double totalDecCDF
) {}