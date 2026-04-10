package com.king.pos.Dto.Response;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class DeviseStatDto {
  private String devise;
  private double total;
}