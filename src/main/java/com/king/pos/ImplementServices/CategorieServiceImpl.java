package com.king.pos.ImplementServices;


import com.king.pos.Dao.CategorieRepository;
import com.king.pos.Dao.ProduitRepository;
import com.king.pos.Dto.CategorieRequest;
import com.king.pos.Dto.Response.CategorieResponse;
import com.king.pos.Entitys.Categorie;
import com.king.pos.Handllers.BusinessException;
import com.king.pos.Handllers.ResourceNotFoundException;
import com.king.pos.Interface.CategorieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategorieServiceImpl implements CategorieService {

    private final CategorieRepository categorieRepository;
    private final ProduitRepository produitRepository;


    @Override
    public CategorieResponse create(CategorieRequest request) {
        if (request.getNom() == null || request.getNom().trim().isEmpty()) {
            throw new BusinessException("Le nom de la catégorie est obligatoire");
        }

        String nom = request.getNom().trim();

        if (categorieRepository.existsByNomIgnoreCase(nom)) {
            throw new BusinessException("Cette catégorie existe déjà");
        }

        Categorie categorie = Categorie.builder()
                .nom(nom)
                .description(request.getDescription())
                .actif(true)
                .build();

        Categorie saved = categorieRepository.save(categorie);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorieResponse> findAll() {
        return categorieRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorieResponse> findAllActive() {
        return categorieRepository.findByActifTrueOrderByNomAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategorieResponse findById(Long id) {
        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));

        return mapToResponse(categorie);
    }

    private CategorieResponse mapToResponse(Categorie categorie) {
        return CategorieResponse.builder()
                .id(categorie.getId())
                .nom(categorie.getNom())
                .description(categorie.getDescription())
                .actif(categorie.getActif())
                .dateCreation(categorie.getDateCreation())
                .build();
    }
@Override
public CategorieResponse update(Long id, CategorieRequest request) {

    Categorie categorie = categorieRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));

    if (request.getNom() == null || request.getNom().trim().isEmpty()) {
        throw new BusinessException("Le nom de la catégorie est obligatoire");
    }

    String nouveauNom = request.getNom().trim();

    categorieRepository.findByNomIgnoreCase(nouveauNom)
            .ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new BusinessException("Une autre catégorie avec ce nom existe déjà");
                }
            });

    // 🔥 Bloquer désactivation si utilisée par des produits
    if (Boolean.FALSE.equals(request.getActif())) {
        boolean utilise = produitRepository.existsByCategorieId(id);

        if (utilise) {
            throw new BusinessException(
                "Impossible de désactiver cette catégorie car elle est utilisée par des produits"
            );
        }
    }

    categorie.setNom(nouveauNom);
    categorie.setDescription(request.getDescription());

    if (request.getActif() != null) {
        categorie.setActif(request.getActif());
    }

    Categorie updated = categorieRepository.save(categorie);

    return mapToResponse(updated);
}
}