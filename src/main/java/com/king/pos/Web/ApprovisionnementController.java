package com.king.pos.Web;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.king.pos.Dao.ProduitRepository;
import com.king.pos.Dao.StockRepository;
import com.king.pos.Dao.VenteRepository;
import com.king.pos.Dto.Response.SuggestionApproResponse;
import com.king.pos.Dto.Response.SuggestionApprovisionnementResponse;
import com.king.pos.Entitys.LigneVente;
import com.king.pos.Entitys.Produit;
import com.king.pos.Entitys.ProduitFournisseur;
import com.king.pos.Entitys.StockProduit;
import com.king.pos.Interface.ApprovisionnementService;

import java.util.*;

@RestController
@RequestMapping("/api/approvisionnement")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ApprovisionnementController {

     private final ApprovisionnementService approvisionnementService;

    @GetMapping("/suggestions")
    public List<SuggestionApproResponse> getSuggestions(
            @RequestParam(required = false) Integer joursCouverture
    ) {
        return approvisionnementService.getSuggestionsReapprovisionnement(joursCouverture);
    }
}