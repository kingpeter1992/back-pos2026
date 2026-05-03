package com.king.pos.ImplementServices;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.king.pos.Dao.TauxChangeRepository;
import com.king.pos.Entitys.TauxChange;
import com.king.pos.request.TauxChangeRequest;
import com.king.pos.request.TauxChangeResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TauxChangeService {

    private final TauxChangeRepository repository;

    @Transactional(readOnly = true)
    public List<TauxChangeResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public TauxChangeResponse getActif() {
        TauxChange taux = repository.findFirstByActifTrueOrderByDateActivationDesc()
                .orElseThrow(() -> new EntityNotFoundException("Aucun taux actif trouvé"));

        return map(taux);
    }

    @Transactional(readOnly = true)
    public BigDecimal getValeurTauxActif() {
        return repository.findFirstByActifTrueOrderByDateActivationDesc()
                .map(TauxChange::getTaux)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional
    public TauxChangeResponse create(TauxChangeRequest request) {
        if (Boolean.TRUE.equals(request.getActif())) {
            desactiverTous();
        }

        TauxChange taux = TauxChange.builder()
                .taux(request.getTaux())
                .actif(Boolean.TRUE.equals(request.getActif()))
                .commentaire(request.getCommentaire())
                .dateCreation(LocalDateTime.now())
                .dateActivation(Boolean.TRUE.equals(request.getActif()) ? LocalDateTime.now() : null)
                .build();

        return map(repository.save(taux));
    }

    @Transactional
    public TauxChangeResponse update(Long id, TauxChangeRequest request) {
        TauxChange taux = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Taux introuvable"));

        if (Boolean.TRUE.equals(request.getActif())) {
            desactiverTous();
            taux.setDateActivation(LocalDateTime.now());
        }

        taux.setTaux(request.getTaux());
        taux.setActif(Boolean.TRUE.equals(request.getActif()));
        taux.setCommentaire(request.getCommentaire());

        return map(repository.save(taux));
    }

    @Transactional
    public TauxChangeResponse activer(Long id) {
        TauxChange taux = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Taux introuvable"));

        desactiverTous();

        taux.setActif(true);
        taux.setDateActivation(LocalDateTime.now());

        return map(repository.save(taux));
    }

    @Transactional
    public void delete(Long id) {
        TauxChange taux = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Taux introuvable"));

        if (Boolean.TRUE.equals(taux.getActif())) {
            throw new IllegalStateException("Impossible de supprimer le taux actif");
        }

        repository.delete(taux);
    }

    private void desactiverTous() {
        repository.findAll().forEach(t -> {
            t.setActif(false);
            repository.save(t);
        });
    }

    private TauxChangeResponse map(TauxChange t) {
        return TauxChangeResponse.builder()
                .id(t.getId())
                .taux(t.getTaux())
                .actif(t.getActif())
                .dateCreation(t.getDateCreation())
                .dateActivation(t.getDateActivation())
                .commentaire(t.getCommentaire())
                .build();
    }
}