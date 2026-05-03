package com.king.pos.Web;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.king.pos.Dto.ProduitRequest;
import com.king.pos.Dto.Response.ProduitResponse;
import com.king.pos.ImplementServices.BarcodeServiceImpl;
import com.king.pos.ImplementServices.ProduitServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProduitController {

    private final ProduitServiceImpl produitService;
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

@GetMapping(value = "/{id}/barcode-image", produces = MediaType.IMAGE_PNG_VALUE)
public ResponseEntity<byte[]> barcodeImage(@PathVariable Long id) {

    String codeBarres = produitService.getCodeBarresById(id);

    if (codeBarres == null || codeBarres.trim().isEmpty()) {
        return ResponseEntity.noContent().build();
    }

    byte[] png = barcodeService.generateBarcodePng(codeBarres.trim(), 420, 120);

    return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(png);
}


    @GetMapping("/{id}")
    public ProduitResponse findById(@PathVariable Long id) {
        return produitService.findById(id);
    }
}
