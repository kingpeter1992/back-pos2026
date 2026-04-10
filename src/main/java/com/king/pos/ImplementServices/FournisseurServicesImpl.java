package com.king.pos.ImplementServices;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.king.pos.Dao.FournisseurRepository;
import com.king.pos.Entitys.Fournisseur;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FournisseurServicesImpl {

 private final FournisseurRepository fournisseurRepository;

    public List<Fournisseur> getAll() {
        return fournisseurRepository.findAll();
    }

    public Fournisseur getById(Long id) {
        return fournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur introuvable : " + id));
    }

    public Fournisseur create(Fournisseur fournisseur) {
        if (fournisseurRepository.existsByNomIgnoreCase(fournisseur.getNom())) {
            throw new RuntimeException("Un fournisseur avec ce nom existe déjà.");
        }
        return fournisseurRepository.save(fournisseur);
    }

    public Fournisseur update(Long id, Fournisseur data) {
        Fournisseur fournisseur = getById(id);

        if (!fournisseur.getNom().equalsIgnoreCase(data.getNom())
                && fournisseurRepository.existsByNomIgnoreCase(data.getNom())) {
            throw new RuntimeException("Un fournisseur avec ce nom existe déjà.");
        }

        fournisseur.setNom(data.getNom());
        fournisseur.setTelephone(data.getTelephone());
        fournisseur.setEmail(data.getEmail());
        fournisseur.setAdresse(data.getAdresse());
        fournisseur.setPays(data.getPays());
        fournisseur.setVille(data.getVille());
        fournisseur.setDescription(data.getDescription());
        fournisseur.setActif(data.getActif() != null ? data.getActif() : fournisseur.getActif());

        return fournisseurRepository.save(fournisseur);
    }

    public void delete(Long id) {
        Fournisseur fournisseur = getById(id);
        fournisseurRepository.delete(fournisseur);
    }

    public List<Fournisseur> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAll();
        }
        return fournisseurRepository
                .findByNomContainingIgnoreCaseOrTelephoneContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword, keyword, keyword
                );
    }
}