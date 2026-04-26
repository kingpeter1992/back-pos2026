package com.king.pos.ImplementServices;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.king.pos.Dao.DepotRepository;
import com.king.pos.Dao.LocatorRepository;
import com.king.pos.Dto.Response.DepotResponse;
import com.king.pos.Dto.Response.LocatorResponse;
import com.king.pos.Entitys.Depot;
import com.king.pos.Entitys.Locator;
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


    public List<DepotResponse> getActifs() {
        return depotRepository.findByActifTrueOrderByNomAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

 private final LocatorRepository locatorRepository;

    public List<LocatorResponse> findByDepot(Long depotId) {
        return locatorRepository.findByDepotId(depotId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private LocatorResponse mapToResponse(Locator locator) {
        return LocatorResponse.builder()
                .id(locator.getId())
                .code(locator.getCode())
                .libelle(locator.getLibelle())
                .depotId(locator.getDepot() != null ? locator.getDepot().getId() : null)
                .build();
    }

    public List<LocatorResponse> getAllLocator() {
        return locatorRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
}
}
