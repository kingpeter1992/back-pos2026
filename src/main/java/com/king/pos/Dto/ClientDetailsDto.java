package com.king.pos.Dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

import com.king.pos.Dto.Response.TransactionMiniDto;

@Getter @Setter
public class ClientDetailsDto {
  private ClientDto client;
  private ClientStatsDto stats;
  private List<TransactionMiniDto> transactions;

}