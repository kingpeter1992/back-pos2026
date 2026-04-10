package com.king.pos.Web;

import com.king.pos.Dto.CategorieRequest;
import com.king.pos.Dto.Response.CategorieResponse;
import com.king.pos.ImplementServices.CategorieServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CategorieController {

    private final CategorieServiceImpl categorieService;

    @PostMapping
    public CategorieResponse create(@RequestBody CategorieRequest request) {
        return categorieService.create(request);
    }
@PutMapping("/{id}")
public CategorieResponse update(@PathVariable Long id, @RequestBody CategorieRequest request) {
    return categorieService.update(id, request);
}
    @GetMapping
    public List<CategorieResponse> findAll() {
        return categorieService.findAll();
    }

    @GetMapping("/actives")
    public List<CategorieResponse> findAllActive() {
        return categorieService.findAllActive();
    }

    @GetMapping("/{id}")
    public CategorieResponse findById(@PathVariable Long id) {
        return categorieService.findById(id);
    }
    
 

}