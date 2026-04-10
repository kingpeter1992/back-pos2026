package com.king.pos.Web;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.king.pos.Dto.ProduitRequest;
import com.king.pos.Dto.Response.ProduitResponse;
import com.king.pos.ImplementServices.BarcodeServiceImpl;
import com.king.pos.Interface.ProduitService;

import java.util.List;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProduitController {

    private final ProduitService produitService;
    private  final  BarcodeServiceImpl barcodeService;

    @PostMapping
    public ProduitResponse create(@RequestBody ProduitRequest request) {
        return produitService.create(request);
    }

    @GetMapping
    public List<ProduitResponse> findAll() {
        return produitService.findAll();
    }

    @PutMapping("/{id}")
    public ProduitResponse update(@PathVariable Long id, @RequestBody ProduitRequest request) {
        return produitService.update(id, request);
    }

    @GetMapping("/barcode/{codeBarres}")
    public ProduitResponse findByBarcode(@PathVariable String codeBarres) {
        return produitService.findByCodeBarres(codeBarres);
    }

@GetMapping("/{id}/barcode-image")
public ResponseEntity<byte[]> barcodeImage(@PathVariable Long id) {
    ProduitResponse produit = produitService.findById(id);

    byte[] png = barcodeService.generateBarcodePng(produit.getCodeBarres(), 420, 120);

    return ResponseEntity.ok()
            .header("Content-Type", "image/png")
            .body(png);
}
    @GetMapping("/{id}")
    public ProduitResponse findById(@PathVariable Long id) {
        return produitService.findById(id);
    }
}
