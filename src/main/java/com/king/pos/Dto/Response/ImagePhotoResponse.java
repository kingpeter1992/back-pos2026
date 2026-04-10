package com.king.pos.Dto.Response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImagePhotoResponse {
    private Long id;
    private String nomFichier;
    private String contentType;
    private String url;
    private Boolean principale;
}