package com.king.pos.Web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.king.pos.Dto.TransactionStockView;
import com.king.pos.Dto.Response.DepotResponse;
import com.king.pos.Dto.Response.ProvisionStockDashboardResponse;
import com.king.pos.Dto.Response.ProvisionStockResponse;
import com.king.pos.Dto.Response.StockAlerteView;
import com.king.pos.Dto.Response.StockProduitView;
import com.king.pos.ImplementServices.DepotServiceImpl;
import com.king.pos.ImplementServices.ProvisionStockServiceImpl;
import com.king.pos.ImplementServices.StockServiceImpl;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@CrossOrigin("*")
public class StockController {

    private final StockServiceImpl stockService;


    @GetMapping
    public List<StockProduitView> getAllStock() {
        return stockService.getAllStock();
    }

    @GetMapping("/alertes")
    public List<StockAlerteView> getAlertesStock() {
        return stockService.getAlertesStock();
    }

 @GetMapping("/mouvements")
public List<TransactionStockView> getAll() {
    return stockService.getAllMouvements();
}
    private final DepotServiceImpl depotService;

  @GetMapping("depos")
    public List<DepotResponse> getAllDepot() {
        return depotService.getAll();
    }

        private final ProvisionStockServiceImpl provisionStockService;

    @GetMapping("/provision")
    public List<ProvisionStockResponse> calculerProvisionStock() {
        return provisionStockService.calculerProvisionStock();
    }

    @GetMapping("/dashboard")
    public ProvisionStockDashboardResponse getDashboardProvisionStock() {
        return provisionStockService.getDashboardProvisionStock();
    }

}
