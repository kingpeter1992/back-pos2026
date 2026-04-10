package com.king.pos.Web;



import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.king.pos.Entitys.Fournisseur;
import com.king.pos.ImplementServices.FournisseurServicesImpl;

import java.util.List;

@RestController
@RequestMapping("/api/fournisseurs")
@CrossOrigin("*")
@RequiredArgsConstructor
public class FournisseurController {

    private final FournisseurServicesImpl fournisseurService;

    @GetMapping
    public List<Fournisseur> getAll(@RequestParam(required = false) String keyword) {
        return fournisseurService.search(keyword);
    }

    @GetMapping("/{id}")
    public Fournisseur getById(@PathVariable Long id) {
        return fournisseurService.getById(id);
    }

    @PostMapping
    public Fournisseur create(@RequestBody Fournisseur fournisseur) {
        return fournisseurService.create(fournisseur);
    }

    @PutMapping("/{id}")
    public Fournisseur update(@PathVariable Long id, @RequestBody Fournisseur fournisseur) {
        return fournisseurService.update(id, fournisseur);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        fournisseurService.delete(id);
    }
}