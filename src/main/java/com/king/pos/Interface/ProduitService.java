package com.king.pos.Interface;


import java.util.List;

import com.king.pos.Dto.ProduitRequest;
import com.king.pos.Dto.Response.ProduitResponse;

public interface ProduitService {
    ProduitResponse create(ProduitRequest request);
    List<ProduitResponse> findAll();
    ProduitResponse findByCodeBarres(String codeBarres);
    ProduitResponse update(Long id, ProduitRequest request);
    ProduitResponse findById(Long id);        
    
}
