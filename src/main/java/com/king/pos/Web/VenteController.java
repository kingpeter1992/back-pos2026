package com.king.pos.Web;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.king.pos.Dto.VenteRequest;
import com.king.pos.Dto.Response.ProduitPosResponse;
import com.king.pos.Dto.Response.VenteResponse;
import com.king.pos.Entitys.Vente;
import com.king.pos.ImplementServices.StockServiceImpl;
import com.king.pos.Interface.VenteService;

@RestController
@RequestMapping("/api/ventes")
@RequiredArgsConstructor
@CrossOrigin("*")
public class VenteController {

    private final VenteService venteService;
    private final StockServiceImpl stockService;

    @PostMapping
    public VenteResponse enregistrer(@RequestBody VenteRequest request) {
        return venteService.enregistrerVente(request);
    }
        @PatchMapping("/{id}/annuler")
        public VenteResponse annulerVente(@PathVariable Long id) {
            return venteService.annulerVente(id);
        }
    @GetMapping
    public List<VenteResponse> getAllVente() {
        return venteService.getAllVente();
    }

    @GetMapping("/pos")
    public List<ProduitPosResponse> getProduitsPos() {
        return stockService.getProduitsPos();
    }
}
