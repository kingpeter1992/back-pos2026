package com.king.pos.ImplementServices;


import com.king.pos.Dao.VenteLotConsommationRepository;
import com.king.pos.Dto.LotConsommationResult;
import com.king.pos.Entitys.LigneVente;
import com.king.pos.Entitys.Vente;
import com.king.pos.Entitys.VenteLotConsommation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VenteLotConsommationService {

    private final VenteLotConsommationRepository repository;

    @Transactional
    public void enregistrerConsommations(
            Vente vente,
            LigneVente ligneVente,
            List<LotConsommationResult> consommations
    ) {
        for (LotConsommationResult c : consommations) {
            VenteLotConsommation entity = VenteLotConsommation.builder()
                    .vente(vente)
                    .ligneVente(ligneVente)
                    .stockLot(c.getStockLot())
                    .quantiteConsommee(c.getQuantiteConsommee())
                    .coutUnitaireAuMomentVente(c.getCoutUnitaireFinal())
                    .datePeremptionAuMomentVente(c.getDatePeremption())
                    .build();

            repository.save(entity);
        }
    }

    public List<VenteLotConsommation> getByVenteId(Long venteId) {
        return repository.findByVenteIdOrderByIdAsc(venteId);
    }

    @Transactional
public void enregistrerConsommationsRetour(
        Vente venteRetour,
        LigneVente ligneVenteRetour,
        List<VenteLotConsommation> consommationsOriginales
) {
    for (VenteLotConsommation c : consommationsOriginales) {
        VenteLotConsommation entity = VenteLotConsommation.builder()
                .vente(venteRetour)
                .ligneVente(ligneVenteRetour)
                .stockLot(c.getStockLot())
                .quantiteConsommee(nvl(c.getQuantiteConsommee()).negate())
                .coutUnitaireAuMomentVente(nvl(c.getCoutUnitaireAuMomentVente()))
                .datePeremptionAuMomentVente(c.getDatePeremptionAuMomentVente())
                .build();

        repository.save(entity);
    }
}

private BigDecimal nvl(BigDecimal value) {
    return value != null ? value : BigDecimal.ZERO;
}
}
