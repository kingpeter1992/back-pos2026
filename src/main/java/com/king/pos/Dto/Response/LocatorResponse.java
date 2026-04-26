package com.king.pos.Dto.Response;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Data @Builder
public class LocatorResponse {
  private Long id;
    private String code;
    private String libelle;
    private Long depotId; 
}
