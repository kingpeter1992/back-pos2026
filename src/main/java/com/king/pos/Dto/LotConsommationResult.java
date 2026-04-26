package com.king.pos.Dto;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.king.pos.Entitys.StockLot;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotConsommationResult {
   private StockLot stockLot;
    private BigDecimal quantiteConsommee;
    private BigDecimal quantiteRestanteLot;
    private BigDecimal coutUnitaireFinal;
    private LocalDate datePeremption;
}