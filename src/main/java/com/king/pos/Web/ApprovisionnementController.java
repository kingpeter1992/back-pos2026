package com.king.pos.Web;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.king.pos.Dao.ProduitRepository;
import com.king.pos.Dao.StockRepository;
import com.king.pos.Dao.VenteRepository;
import com.king.pos.Dto.Response.SuggestionApprovisionnementResponse;
import com.king.pos.Entitys.LigneVente;
import com.king.pos.Entitys.Produit;
import com.king.pos.Entitys.ProduitFournisseur;
import com.king.pos.Entitys.StockProduit;

import java.util.*;

@RestController
@RequestMapping("/api/approvisionnements")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ApprovisionnementController {

    private final ProduitRepository produitRepository;
    private final StockRepository stockRepository;
    private final VenteRepository venteRepository;

    @GetMapping("/suggestions")
public List<Map<String, Object>> suggestions() {
    List<Map<String, Object>> result = new ArrayList<>();

    var ventes = venteRepository.findAll();
    var dateLimite = java.time.LocalDateTime.now().minusDays(30);

    for (Produit produit : produitRepository.findAll()) {

        int quantiteVendue = ventes.stream()
                .filter(v -> v.getDateVente() != null && !v.getDateVente().isBefore(dateLimite))
                .flatMap(v -> v.getLignes().stream())
                .filter(l -> l.getProduit().getId().equals(produit.getId()))
                .mapToInt(LigneVente::getQuantite)
                .sum();

        double moyenneJour = quantiteVendue / 30.0;

        ProduitFournisseur pfPrincipal = produit.getProduitFournisseurs().stream()
                .filter(pf -> Boolean.TRUE.equals(pf.getFournisseurPrincipal()))
                .findFirst()
                .orElse(null);

        int delai = (pfPrincipal != null && pfPrincipal.getDelaiLivraisonJours() != null)
                ? pfPrincipal.getDelaiLivraisonJours()
                : 3;

        String fournisseurPrincipalNom = (pfPrincipal != null && pfPrincipal.getFournisseur() != null)
                ? pfPrincipal.getFournisseur().getNom()
                : null;

        StockProduit stock = stockRepository.findByProduitId(produit.getId()).orElse(null);
        int stockActuel = stock != null 
        ? stock.getQuantiteDisponible().intValue() 
        : 0;

        int quantiteACommander = (int) Math.ceil((moyenneJour * delai) + produit.getStockMinimum() - stockActuel);
        if (quantiteACommander < 0) {
            quantiteACommander = 0;
        }

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("produitId", produit.getId());
        item.put("codeBarres", produit.getCodeBarres());
        item.put("produit", produit.getNom());
        item.put("stockActuel", stockActuel);
        item.put("stockMinimum", produit.getStockMinimum());
        item.put("totalVendu30j", quantiteVendue);
        item.put("moyenneJour", moyenneJour);
        item.put("delaiLivraison", delai);
        item.put("fournisseurPrincipal", fournisseurPrincipalNom);
        item.put("quantiteACommander", quantiteACommander);

        result.add(item);
    }

    return result;
}
}