package com.king.pos.ImplementServices;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.king.pos.Dao.DepotRepository;
import com.king.pos.Dto.Response.DepotResponse;
import com.king.pos.Entitys.Depot;
import com.king.pos.Interface.DepotService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepotServiceImpl implements DepotService {

    private final DepotRepository depotRepository;

    @Override
    public List<DepotResponse> getAll() {
        return depotRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public DepotResponse getById(Long id) {
        Depot depot = depotRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dépôt introuvable avec l'id : " + id));

        return mapToResponse(depot);
    }

    private DepotResponse mapToResponse(Depot depot) {
        DepotResponse response = new DepotResponse();
        response.setId(depot.getId());
        response.setNom(depot.getNom());
        return response;
    }
}
