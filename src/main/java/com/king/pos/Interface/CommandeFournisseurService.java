package com.king.pos.Interface;

import java.util.List;

import com.king.pos.Dto.CreateCommandeAchatRequest;
import com.king.pos.Dto.Response.CommandeAchatResponse;
import com.king.pos.Dto.Response.CommandeDashboardResponse;

public interface CommandeFournisseurService {
    CommandeAchatResponse valider(Long id);
    CommandeAchatResponse create(CreateCommandeAchatRequest request);
    CommandeAchatResponse update(Long id, CreateCommandeAchatRequest request);
    List<CommandeAchatResponse> findAll();
    CommandeDashboardResponse getDashboard();

    
}