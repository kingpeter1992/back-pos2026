package com.king.pos.Interface;


import com.king.pos.Dto.*;
import com.king.pos.Dto.Response.TarifCategorieProduitResponse;
import com.king.pos.Dto.Response.TarifVenteResponse;
import com.king.pos.Dto.Response.TarificationResponse;

import java.util.List;

public interface TarifVenteService {

    TarifVenteResponse createTarif(TarifVenteRequest request);

    TarifVenteResponse updateTarif(Long id, TarifVenteRequest request);

    List<TarifVenteResponse> getAll();

    List<TarifVenteResponse> getAllActifs();

    TarifCategorieProduitResponse createOrUpdateRegle(TarifCategorieProduitRequest request);

    List<TarifCategorieProduitResponse> getAllRegles();

    List<TarifCategorieProduitResponse> getReglesByTarif(Long tarifVenteId);

    TarificationResponse calculerPrix(TarificationProduitRequest request);

    List<TarificationResponse> calculerPrixLot(TarificationLotRequest request);

    TarifVenteResponse toggleActif(Long id);

    TarifVenteResponse definirParDefaut(Long id);

    void deleteTarif(Long id);
}