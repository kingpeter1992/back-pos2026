package com.king.pos.Web;

import com.king.pos.Dao.LocatorRepository;
import com.king.pos.Dto.Response.DepotResponse;
import com.king.pos.Dto.Response.LocatorResponse;
import com.king.pos.ImplementServices.DepotServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/depots")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DepotController {

    private final DepotServiceImpl depotService;
    private LocatorRepository locatorRepository;

    @GetMapping
    public List<DepotResponse> getAll() {
        return depotService.getAll();
    }

    @GetMapping("/actifs")
    public List<DepotResponse> getActifs() {
        return depotService.getActifs();
    }

    @GetMapping("/{id}")
    public DepotResponse getById(@PathVariable Long id) {
        return depotService.getById(id);
    }

@GetMapping("/locatorbydepot/{depotId}")
public List<LocatorResponse> findByDepot(@PathVariable Long depotId) {
    return depotService.findByDepot(depotId);
}

@GetMapping("/locator")
public List<LocatorResponse> getAllLocator() {
    return depotService.getAllLocator();
}

}