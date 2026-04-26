package com.king.pos.Interface;

import java.util.List;
import java.util.Optional;

import com.king.pos.Dto.TransactionStockView;
import com.king.pos.Dto.Response.ProduitPosResponse;
import com.king.pos.Dto.Response.StockAlerteView;
import com.king.pos.Dto.Response.StockProduitView;
import com.king.pos.Entitys.Produit;
import com.king.pos.Entitys.StockProduit;

public interface StockService {
    void ajouterStock(Long produitId, Integer quantite, String reference, String commentaire);
    void retirerStock(Long produitId, Integer quantite, String reference, String commentaire);
    List<StockProduitView> getAllStock();
    List<StockAlerteView> getAlertesStock();
    Optional<StockProduit> getStockByProduitId(Long produitId);
    Optional<StockProduit> getStockByProduitIdAndDepotId(Long produitId, Long depotId);
    void validateQuantitesProduit(Produit produit);
   // String resolveStatutStock(Produit produit, StockProduit stock);
    List<ProduitPosResponse> getProduitsPos();
    List<TransactionStockView> getAllMouvements();
}