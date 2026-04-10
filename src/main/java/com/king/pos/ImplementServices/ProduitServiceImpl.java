package com.king.pos.ImplementServices;

import com.king.pos.Dao.CategorieRepository;
import com.king.pos.Dao.FournisseurRepository;
import com.king.pos.Dao.ProduitRepository;
import com.king.pos.Dao.StockRepository;
import com.king.pos.Dto.ImagePhotoRequest;
import com.king.pos.Dto.ProduitFournisseurRequest;
import com.king.pos.Dto.ProduitRequest;
import com.king.pos.Dto.Response.ImagePhotoResponse;
import com.king.pos.Dto.Response.ProduitFournisseurResponse;
import com.king.pos.Dto.Response.ProduitResponse;
import com.king.pos.Entitys.Categorie;
import com.king.pos.Entitys.Fournisseur;
import com.king.pos.Entitys.ImagePhoto;
import com.king.pos.Entitys.Produit;
import com.king.pos.Entitys.ProduitFournisseur;
import com.king.pos.Entitys.StockProduit;
import com.king.pos.Handllers.BusinessException;
import com.king.pos.Handllers.ResourceNotFoundException;
import com.king.pos.Interface.ProduitService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Transactional
public class ProduitServiceImpl implements ProduitService {

    private final ProduitRepository produitRepository;
    private final FournisseurRepository fournisseurRepository;
    private final CategorieRepository categorieRepository;
    private final StockRepository stockRepository;

 @Override
public ProduitResponse create(ProduitRequest request) {
    
    Categorie categorie = null;
    if (request.getCategorieId() != null) {
        categorie = categorieRepository.findById(request.getCategorieId())
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));
    }

    String codeBarres = (request.getCodeBarres() != null && !request.getCodeBarres().isBlank())
            ? request.getCodeBarres().trim()
            : generateUniqueBarcode();

    if (produitRepository.existsByCodeBarres(codeBarres)) {
        throw new BusinessException("Ce code-barres existe déjà");
    }

    Produit produit = Produit.builder()
            .codeBarres(codeBarres)
            .nom(request.getNom())
            .description(request.getDescription())
            .categorie(categorie)
            .prixVente(request.getPrixVente())
            .stockMinimum(request.getStockMinimum())
            .stockMaximum(request.getStockMaximum())
            .actif(true)
            .build();

    if (request.getImages() != null && !request.getImages().isEmpty()) {
        List<ImagePhoto> images = new ArrayList<>();
        for (ImagePhotoRequest img : request.getImages()) {
            images.add(ImagePhoto.builder()
                    .produit(produit)
                    .nomFichier(img.getNomFichier())
                    .contentType(img.getContentType())
                    .url(img.getUrl())
                    .principale(Boolean.TRUE.equals(img.getPrincipale()))
                    .build());
        }
        produit.setImages(images);
    }

    if (request.getFournisseurs() != null && !request.getFournisseurs().isEmpty()) {
        long nbPrincipal = request.getFournisseurs().stream()
                .filter(f -> Boolean.TRUE.equals(f.getFournisseurPrincipal()))
                .count();

        if (nbPrincipal > 1) {
            throw new BusinessException("Un seul fournisseur principal est autorisé par produit");
        }

        List<ProduitFournisseur> liaisons = new ArrayList<>();

        for (ProduitFournisseurRequest f : request.getFournisseurs()) {
            Fournisseur fournisseur = fournisseurRepository.findById(f.getFournisseurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fournisseur introuvable : " + f.getFournisseurId()));

            liaisons.add(ProduitFournisseur.builder()
                    .produit(produit)
                    .fournisseur(fournisseur)
                    .referenceFournisseur(f.getReferenceFournisseur())
                    .prixAchat(f.getPrixAchat())
                    .delaiLivraisonJours(f.getDelaiLivraisonJours())
                    .quantiteMinCommande(f.getQuantiteMinCommande())
                    .fournisseurPrincipal(Boolean.TRUE.equals(f.getFournisseurPrincipal()))
                    .actif(true)
                    .build());
        }

        produit.setProduitFournisseurs(liaisons);
    }

    Produit savedProduit = produitRepository.save(produit);

    return mapToResponse(savedProduit);
}

private String generateUniqueBarcode() {
    String code;
    do {
        code = "POS-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    } while (produitRepository.existsByCodeBarres(code));
    return code;
}

    @Override
    @Transactional(readOnly = true)
    public List<ProduitResponse> findAll() {
        return produitRepository.findAll().stream()
                .map(p -> {
                    return mapToResponse(p);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProduitResponse findByCodeBarres(String codeBarres) {
        Produit produit = produitRepository.findByCodeBarres(codeBarres).get();

        return mapToResponse(produit);
    }

    private ProduitResponse mapToResponse(Produit produit) {
    return ProduitResponse.builder()
            .id(produit.getId())
            .codeBarres(produit.getCodeBarres())
            .nom(produit.getNom())
            .description(produit.getDescription())
            .categorieId(produit.getCategorie() != null ? produit.getCategorie().getId() : null)
            .categorieNom(produit.getCategorie() != null ? produit.getCategorie().getNom() : null)
            .prixVente(produit.getPrixVente())
            .stockMinimum(produit.getStockMinimum())
            .stockMaximum(produit.getStockMaximum())
            .actif(produit.getActif())
            .dateCreation(produit.getDateCreation())
            .images(
                    produit.getImages().stream().map(img ->
                            ImagePhotoResponse.builder()
                                    .id(img.getId())
                                    .nomFichier(img.getNomFichier())
                                    .contentType(img.getContentType())
                                    .url(img.getUrl())
                                    .principale(img.getPrincipale())
                                    .build()
                    ).toList()
            )
            .fournisseurs(
                    produit.getProduitFournisseurs().stream().map(pf ->
                            ProduitFournisseurResponse.builder()
                                    .id(pf.getId())
                                    .fournisseurId(pf.getFournisseur().getId())
                                    .fournisseurNom(pf.getFournisseur().getNom())
                                    .referenceFournisseur(pf.getReferenceFournisseur())
                                    .prixAchat(pf.getPrixAchat())
                                    .delaiLivraisonJours(pf.getDelaiLivraisonJours())
                                    .quantiteMinCommande(pf.getQuantiteMinCommande())
                                    .fournisseurPrincipal(pf.getFournisseurPrincipal())
                                    .actif(pf.getActif())
                                    .build()
                    ).toList()
            )
            .build();
}

@Override
public ProduitResponse update(Long id, ProduitRequest request) {

    Produit produit = produitRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));

    // 🔹 Validation nom
    if (request.getNom() == null || request.getNom().trim().isEmpty()) {
        throw new BusinessException("Le nom du produit est obligatoire");
    }

    // 🔹 Validation prix
    if (request.getPrixVente() == null || request.getPrixVente().doubleValue() < 0) {
        throw new BusinessException("Le prix de vente est invalide");
    }

    // 🔹 Gestion code-barres
    String codeBarres = request.getCodeBarres();

    if (codeBarres == null || codeBarres.isBlank()) {
        codeBarres = produit.getCodeBarres(); // garder l'ancien
    } else {
        codeBarres = codeBarres.trim();

        produitRepository.findByCodeBarres(codeBarres)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BusinessException("Ce code-barres est déjà utilisé");
                    }
                });
    }

    // 🔹 Catégorie
    Categorie categorie = null;
    if (request.getCategorieId() != null) {
        categorie = categorieRepository.findById(request.getCategorieId())
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));
    }

    // 🔹 Mise à jour champs simples
    produit.setCodeBarres(codeBarres);
    produit.setNom(request.getNom().trim());
    produit.setDescription(request.getDescription());
    produit.setCategorie(categorie);
    produit.setPrixVente(request.getPrixVente());
    produit.setStockMinimum(request.getStockMinimum() != null ? request.getStockMinimum() : 0);
    produit.setStockMaximum(request.getStockMaximum() != null ? request.getStockMaximum() : 0);


    // 🔥 Actif / inactif
    if (request.getActif() != null) {
        produit.setActif(request.getActif());
    }

    // 🔥 Gestion des images (remplacement complet)
    produit.getImages().clear();

    if (request.getImages() != null && !request.getImages().isEmpty()) {
        boolean hasMain = false;

        for (ImagePhotoRequest img : request.getImages()) {

            boolean principale = Boolean.TRUE.equals(img.getPrincipale());

            if (principale) {
                hasMain = true;
            }

            produit.getImages().add(
                    ImagePhoto.builder()
                            .produit(produit)
                            .nomFichier(img.getNomFichier())
                            .contentType(img.getContentType())
                            .url(img.getUrl())
                            .principale(principale)
                            .build()
            );
        }

        // 🔥 si aucune image principale → la première devient principale
        if (!hasMain && !produit.getImages().isEmpty()) {
            produit.getImages().get(0).setPrincipale(true);
        }
    }

    Produit updated = produitRepository.save(produit);


    return mapToResponse(updated);
}

@Override
@Transactional(readOnly = true)
public ProduitResponse findById(Long id) {
    Produit produit = produitRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));

    return mapToResponse(produit);
}


  
}