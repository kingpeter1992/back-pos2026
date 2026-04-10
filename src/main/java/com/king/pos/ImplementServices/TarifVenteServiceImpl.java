package com.king.pos.ImplementServices;

import com.king.pos.Dao.CategorieRepository;
import com.king.pos.Dao.ProduitRepository;
import com.king.pos.Dao.StockRepository;
import com.king.pos.Dao.TarifCategorieProduitRepository;
import com.king.pos.Dao.TarifVenteRepository;
import com.king.pos.Dto.TarifCategorieProduitRequest;
import com.king.pos.Dto.TarifVenteRequest;
import com.king.pos.Dto.TarificationLotRequest;
import com.king.pos.Dto.TarificationProduitRequest;
import com.king.pos.Dto.Response.TarifCategorieProduitResponse;
import com.king.pos.Dto.Response.TarifVenteResponse;
import com.king.pos.Dto.Response.TarificationResponse;
import com.king.pos.Entitys.*;
import com.king.pos.Interface.TarifVenteService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class TarifVenteServiceImpl implements TarifVenteService {

    private final TarifVenteRepository tarifVenteRepository;
    private final TarifCategorieProduitRepository tarifCategorieProduitRepository;
    private final CategorieRepository categorieRepository;
    private final ProduitRepository produitRepository;
    private final StockRepository stockProduitRepository;

    private static final BigDecimal CENT = new BigDecimal("100");

    @Override
public TarifVenteResponse createTarif(TarifVenteRequest request) {
    if (request.getCode() == null || request.getCode().isBlank()) {
        throw new IllegalStateException("Le code du tarif est obligatoire.");
    }
    if (request.getNom() == null || request.getNom().isBlank()) {
        throw new IllegalStateException("Le nom du tarif est obligatoire.");
    }

    tarifVenteRepository.findByCodeIgnoreCase(request.getCode().trim())
            .ifPresent(t -> {
                throw new IllegalStateException("Un tarif existe déjà avec ce code.");
            });

    boolean parDefaut = request.getParDefaut() != null && request.getParDefaut();

    if (parDefaut) {
        tarifVenteRepository.clearDefaultTarif();
    }

    TarifVente entity = TarifVente.builder()
            .code(request.getCode().trim())
            .nom(request.getNom().trim())
            .description(request.getDescription())
            .actif(request.getActif() != null ? request.getActif() : true)
            .parDefaut(parDefaut)
            .build();

    return mapTarif(tarifVenteRepository.save(entity));
}
   @Override
public TarifVenteResponse updateTarif(Long id, TarifVenteRequest request) {
    TarifVente entity = tarifVenteRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Tarif introuvable."));

    if (request.getCode() != null && !request.getCode().isBlank()) {
        tarifVenteRepository.findByCodeIgnoreCase(request.getCode().trim())
                .filter(t -> !Objects.equals(t.getId(), id))
                .ifPresent(t -> {
                    throw new IllegalStateException("Un autre tarif existe déjà avec ce code.");
                });
        entity.setCode(request.getCode().trim());
    }

    if (request.getNom() != null && !request.getNom().isBlank()) {
        entity.setNom(request.getNom().trim());
    }

    entity.setDescription(request.getDescription());

    if (request.getActif() != null) {
        entity.setActif(request.getActif());
    }

    if (request.getParDefaut() != null) {
        if (request.getParDefaut()) {
            tarifVenteRepository.clearDefaultTarif();
            entity.setParDefaut(true);
            entity.setActif(true);
        } else {
            entity.setParDefaut(false);
        }
    }

    return mapTarif(tarifVenteRepository.save(entity));
}
    @Override
    @Transactional(readOnly = true)
    public List<TarifVenteResponse> getAll() {
        return tarifVenteRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(TarifVente::getNom, String.CASE_INSENSITIVE_ORDER))
                .map(this::mapTarif)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TarifVenteResponse> getAllActifs() {
        return tarifVenteRepository.findByActifTrueOrderByNomAsc()
                .stream()
                .map(this::mapTarif)
                .toList();
    }

    @Override
    public TarifCategorieProduitResponse createOrUpdateRegle(TarifCategorieProduitRequest request) {
        if (request.getTarifVenteId() == null) {
            throw new IllegalStateException("Le tarif de vente est obligatoire.");
        }
        if (request.getCategorieId() == null) {
            throw new IllegalStateException("La catégorie est obligatoire.");
        }

        TarifVente tarif = tarifVenteRepository.findById(request.getTarifVenteId())
                .orElseThrow(() -> new EntityNotFoundException("Tarif de vente introuvable."));

        Categorie categorie = categorieRepository.findById(request.getCategorieId())
                .orElseThrow(() -> new EntityNotFoundException("Catégorie introuvable."));

        BigDecimal tauxMarge = nvl(request.getTauxMarge());
        BigDecimal tauxRemiseMax = nvl(request.getTauxRemiseMax());

        TarifCategorieProduit regle = tarifCategorieProduitRepository
                .findByTarifVenteIdAndCategorieIdAndActifTrue(tarif.getId(), categorie.getId())
                .orElseGet(TarifCategorieProduit::new);

        regle.setTarifVente(tarif);
        regle.setCategorie(categorie);
        regle.setTauxMarge(tauxMarge);
        regle.setTauxRemiseMax(tauxRemiseMax);
        regle.setActif(request.getActif() != null ? request.getActif() : true);
        regle.setModeArrondi(
                request.getModeArrondi() != null && !request.getModeArrondi().isBlank()
                        ? request.getModeArrondi().trim().toUpperCase()
                        : "AUCUN"
        );
        tarifCategorieProduitRepository.save(regle);

        return mapRegle(regle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TarifCategorieProduitResponse> getAllRegles() {
        return tarifCategorieProduitRepository.findAll()
                .stream()
                .map(this::mapRegle)
                .sorted(Comparator.comparing(TarifCategorieProduitResponse::getTarifNom, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(TarifCategorieProduitResponse::getCategorieNom, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TarifCategorieProduitResponse> getReglesByTarif(Long tarifVenteId) {
        return tarifCategorieProduitRepository.findByTarifVenteIdOrderByCategorieNomAsc(tarifVenteId)
                .stream()
                .map(this::mapRegle)
                .toList();
    }

    
    @Override
    @Transactional(readOnly = true)
    public TarificationResponse calculerPrix(TarificationProduitRequest request) {
        if (request.getProduitId() == null) {
            throw new IllegalStateException("Le produit est obligatoire.");
        }
        if (request.getTarifVenteId() == null) {
            throw new IllegalStateException("Le tarif de vente est obligatoire.");
        }

        Produit produit = produitRepository.findById(request.getProduitId())
                .orElseThrow(() -> new EntityNotFoundException("Produit introuvable."));

        if (produit.getCategorie() == null) {
            throw new IllegalStateException("Le produit n'a pas de catégorie.");
        }

        TarifVente tarif = tarifVenteRepository.findById(request.getTarifVenteId())
                .orElseThrow(() -> new EntityNotFoundException("Tarif introuvable."));

        TarifCategorieProduit regle = tarifCategorieProduitRepository
                .findByTarifVenteIdAndCategorieIdAndActifTrue(tarif.getId(), produit.getCategorie().getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Aucune règle tarifaire active trouvée pour la catégorie [" +
                        produit.getCategorie().getNom() + "] et le tarif [" + tarif.getNom() + "]."
                ));

        BigDecimal pmp = getPmpProduit(produit.getId());
        BigDecimal stockDisponible = getStockProduit(produit.getId());

        BigDecimal tauxMarge = nvl(regle.getTauxMarge());
        BigDecimal tauxRemiseMax = nvl(regle.getTauxRemiseMax());
        BigDecimal tauxRemiseAppliquee = nvl(request.getTauxRemiseSaisie());

        if (tauxRemiseAppliquee.compareTo(tauxRemiseMax) > 0) {
            throw new IllegalStateException("La remise saisie dépasse la remise maximale autorisée.");
        }

        BigDecimal prixBrut = pmp.multiply(
                BigDecimal.ONE.add(tauxMarge.divide(CENT, 8, RoundingMode.HALF_UP))
        );

        BigDecimal montantRemise = prixBrut.multiply(
                tauxRemiseAppliquee.divide(CENT, 8, RoundingMode.HALF_UP)
        );

        BigDecimal prixNet = prixBrut.subtract(montantRemise);
        prixNet = appliquerArrondi(prixNet, regle.getModeArrondi());

        if (prixNet.compareTo(pmp) < 0) {
            throw new IllegalStateException("Le prix final ne peut pas être inférieur au PMP.");
        }

        return TarificationResponse.builder()
                .produitId(produit.getId())
                .produitNom(produit.getDescription())
                .codeBarres(produit.getCodeBarres())
                .categorieId(produit.getCategorie().getId())
                .categorieNom(produit.getCategorie().getNom())
                .tarifVenteId(tarif.getId())
                .tarifCode(tarif.getCode())
                .tarifNom(tarif.getNom())
                .pmp(scale6(pmp))
                .tauxMarge(scale4(tauxMarge))
                .tauxRemiseMax(scale4(tauxRemiseMax))
                .tauxRemiseAppliquee(scale4(tauxRemiseAppliquee))
                .prixBrut(scale6(prixBrut))
                .montantRemise(scale6(montantRemise))
                .prixNet(scale6(prixNet))
                .modeArrondi(regle.getModeArrondi())
                .stockDisponible(scale3(stockDisponible))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TarificationResponse> calculerPrixLot(TarificationLotRequest request) {
        if (request.getTarifVenteId() == null) {
            throw new IllegalStateException("Le tarif de vente est obligatoire.");
        }
        if (request.getProduitIds() == null || request.getProduitIds().isEmpty()) {
            return Collections.emptyList();
        }

        List<TarificationResponse> results = new ArrayList<>();
        for (Long produitId : request.getProduitIds()) {
            TarificationProduitRequest item = new TarificationProduitRequest();
            item.setProduitId(produitId);
            item.setTarifVenteId(request.getTarifVenteId());
            item.setTauxRemiseSaisie(BigDecimal.ZERO);
            results.add(calculerPrix(item));
        }
        return results;
    }

    private BigDecimal getPmpProduit(Long produitId) {
        List<Object[]> rows = stockProduitRepository.findStockAndPmpByProduit();
        for (Object[] row : rows) {
            Long id = convertToLong(row[0]);
            if (Objects.equals(id, produitId)) {
                return convertToBigDecimal(row[2]);
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getStockProduit(Long produitId) {
        List<Object[]> rows = stockProduitRepository.findStockAndPmpByProduit();
        for (Object[] row : rows) {
            Long id = convertToLong(row[0]);
            if (Objects.equals(id, produitId)) {
                return convertToBigDecimal(row[1]);
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal appliquerArrondi(BigDecimal montant, String mode) {
        if (montant == null) return BigDecimal.ZERO;

        String m = (mode == null || mode.isBlank()) ? "AUCUN" : mode.trim().toUpperCase();

        return switch (m) {
            case "ENTIER_SUP" -> montant.setScale(0, RoundingMode.UP);
            case "MULTIPLE_10" -> arrondiMultiple(montant, new BigDecimal("10"));
            case "MULTIPLE_50" -> arrondiMultiple(montant, new BigDecimal("50"));
            case "MULTIPLE_100" -> arrondiMultiple(montant, new BigDecimal("100"));
            default -> montant.setScale(2, RoundingMode.HALF_UP);
        };
    }

    private BigDecimal arrondiMultiple(BigDecimal montant, BigDecimal multiple) {
        return montant.divide(multiple, 0, RoundingMode.UP).multiply(multiple);
    }

  private TarifVenteResponse mapTarif(TarifVente t) {
    return TarifVenteResponse.builder()
            .id(t.getId())
            .code(t.getCode())
            .nom(t.getNom())
            .description(t.getDescription())
            .actif(t.getActif())
            .parDefaut(t.getParDefaut())
            .dateCreation(t.getDateCreation())
            .build();
}

    private TarifCategorieProduitResponse mapRegle(TarifCategorieProduit r) {
        return TarifCategorieProduitResponse.builder()
                .id(r.getId())
                .tarifVenteId(r.getTarifVente().getId())
                .tarifCode(r.getTarifVente().getCode())
                .tarifNom(r.getTarifVente().getNom())
                .categorieId(r.getCategorie().getId())
                .categorieNom(r.getCategorie().getNom())
                .tauxMarge(scale4(r.getTauxMarge()))
                .tauxRemiseMax(scale4(r.getTauxRemiseMax()))
                .actif(r.getActif())
                .modeArrondi(r.getModeArrondi())
                .build();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal scale6(BigDecimal value) {
        return nvl(value).setScale(6, RoundingMode.HALF_UP);
    }

    private BigDecimal scale4(BigDecimal value) {
        return nvl(value).setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal scale3(BigDecimal value) {
        return nvl(value).setScale(3, RoundingMode.HALF_UP);
    }

    private Long convertToLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Integer i) return i.longValue();
        if (value instanceof Number n) return n.longValue();
        return Long.valueOf(value.toString());
    }

    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Double d) return BigDecimal.valueOf(d);
        if (value instanceof Float f) return BigDecimal.valueOf(f);
        if (value instanceof Integer i) return BigDecimal.valueOf(i);
        if (value instanceof Long l) return BigDecimal.valueOf(l);
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(value.toString());
    }

@Override
public TarifVenteResponse toggleActif(Long id) {
    TarifVente tarif = tarifVenteRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Tarif de vente introuvable."));            
    tarif.setActif(!Boolean.TRUE.equals(tarif.getActif()));
    TarifVente saved = tarifVenteRepository.save(tarif);

    return mapTarif(saved);
}

 @Override
public TarifVenteResponse definirParDefaut(Long id) {
    TarifVente tarif = tarifVenteRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Tarif de vente introuvable."));

    tarifVenteRepository.clearDefaultTarif();
    tarif.setParDefaut(true);

    if (tarif.getActif() == null || !tarif.getActif()) {
        tarif.setActif(true);
    }

    TarifVente saved = tarifVenteRepository.save(tarif);
    return mapTarif(saved);
}

    @Override
    public void deleteTarif(Long id) {
        TarifVente tarif = tarifVenteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarif de vente introuvable."));

        tarifVenteRepository.delete(tarif);
    }

    
}
