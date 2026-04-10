package com.king.pos.Dto.Response;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CategoryStatDto {
  private String category;
  private double total;
}