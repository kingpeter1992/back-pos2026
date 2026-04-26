package com.king.pos.Web;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.king.pos.Dto.ReceptionLocatorRequest;
import com.king.pos.Dto.Response.ReceptionLocatorPreparationResponse;
import com.king.pos.ImplementServices.ReceptionLocatorServiceImpl;

@RestController
@RequestMapping("/api/receptions-locators")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ReceptionLocatorController {

    private final ReceptionLocatorServiceImpl receptionLocatorService;

    @GetMapping("/{receptionId}")
    public ResponseEntity<ReceptionLocatorPreparationResponse> getPreparation(
            @PathVariable Long receptionId
    ) {
        return ResponseEntity.ok(receptionLocatorService.getPreparationByReceptionId(receptionId));
    }

    @PostMapping("/{receptionId}")
    public ResponseEntity<Void> affecterLocators(
            @PathVariable Long receptionId,
            @RequestBody ReceptionLocatorRequest request
    ) {
        receptionLocatorService.affecterLocators(receptionId, request);
        return ResponseEntity.ok().build();
    }
}
