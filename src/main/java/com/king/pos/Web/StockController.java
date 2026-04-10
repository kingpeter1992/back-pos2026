package com.king.pos.Web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.king.pos.Dto.Response.MouvementStockView;
import com.king.pos.Dto.Response.StockAlerteView;
import com.king.pos.Dto.Response.StockProduitView;
import com.king.pos.Interface.StockService;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@CrossOrigin("*")
public class StockController {

    private final StockService stockService;

    @GetMapping
    public List<StockProduitView> getAllStock() {
        return stockService.getAllStock();
    }

    @GetMapping("/alertes")
    public List<StockAlerteView> getAlertesStock() {
        return stockService.getAlertesStock();
    }

    @GetMapping("/mouvements")
    public List<MouvementStockView> getAll() {
        return stockService.getAllMouvements();
    }
}
