package com.king.pos.Dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.pos.Entitys.ImagePhoto;

import java.util.List;

public interface ImagePhotoRepository extends JpaRepository<ImagePhoto, Long> {
    List<ImagePhoto> findByProduitId(Long produitId);
}