package com.king.pos.Dto;


import lombok.Data;

@Data
public class ImagePhotoRequest {
    private String nomFichier;
    private String contentType;
    private String url;
    private Boolean principale;
}