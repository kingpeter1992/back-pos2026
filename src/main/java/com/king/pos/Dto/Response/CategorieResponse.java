package com.king.pos.Dto.Response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CategorieResponse {
    private Long id;
    private String nom;
    private String description;
    private Boolean actif;
    private LocalDateTime dateCreation;
}
