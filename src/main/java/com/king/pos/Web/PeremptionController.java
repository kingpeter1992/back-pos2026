package com.king.pos.Web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.king.pos.Dao.AlertePeremptionResponse;
import com.king.pos.Dto.Response.DashboardPeremptionResponse;
import com.king.pos.Dto.Response.StockLotResponse;
import com.king.pos.ImplementServices.PeremptionQueryService;
import com.king.pos.ImplementServices.PeremptionService;
import com.king.pos.ImplementServices.StockLotService;

import java.util.List;

@RestController
@RequestMapping("/api/peremption")
@RequiredArgsConstructor
public class PeremptionController {
    private final StockLotService stockLotService;

    private final PeremptionQueryService peremptionQueryService;
    private final PeremptionService peremptionService;

    @GetMapping("/alertes")
    public ResponseEntity<List<AlertePeremptionResponse>> getAlertes() {
        return ResponseEntity.ok(peremptionQueryService.getAlertes());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardPeremptionResponse> getDashboard() {
        return ResponseEntity.ok(peremptionQueryService.getDashboard());
    }

    @PostMapping("/recalculer")
    public ResponseEntity<Void> recalculerStatuts() {
        peremptionService.recalculerTousLesStatuts();
        return ResponseEntity.ok().build();
    }

    @GetMapping("lot")
    public List<StockLotResponse> getAllstockLotService() {
        return stockLotService.getAll();
    }

}