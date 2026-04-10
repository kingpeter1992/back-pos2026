package com.king.pos.Dto;

import lombok.Data;

import java.util.List;

@Data
public class TarificationLotRequest {
    private Long tarifVenteId;
    private List<Long> produitIds;
}
