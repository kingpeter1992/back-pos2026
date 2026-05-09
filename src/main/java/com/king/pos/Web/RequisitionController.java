package com.king.pos.Web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.king.pos.Dao.RequisitionResponse;
import com.king.pos.Dto.RequisitionCreateRequest;
import com.king.pos.Entitys.RequisitionHistorique;
import com.king.pos.ImplementServices.RequisitionService;


import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/requisitions")
@RequiredArgsConstructor
public class RequisitionController {

    private final RequisitionService service;

    @PostMapping("/demande")
    public void enregistrer(@RequestBody RequisitionCreateRequest req) {
        service.enregistrerDemande(
                req.getProduitId(),
                req.getDepotId(),
                null, 
                req.getQuantite(),
                req.getType(),
                req.getCreePar()
        );
    }

    @GetMapping
    public List<RequisitionResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/historique")
    public List<RequisitionHistorique> getHistorique(
            @RequestParam LocalDate dateFrom,
            @RequestParam LocalDate dateTo
    ) {
        return service.getHistorique(dateFrom, dateTo);
    }
}