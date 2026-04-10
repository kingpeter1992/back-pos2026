package com.king.pos.Interface;

import java.util.List;

import com.king.pos.Dto.CategorieRequest;
import com.king.pos.Dto.Response.CategorieResponse;

public interface CategorieService {
     CategorieResponse create(CategorieRequest request);
    List<CategorieResponse> findAll();
    List<CategorieResponse> findAllActive();
    CategorieResponse findById(Long id);
    CategorieResponse update(Long id, CategorieRequest request);
}
