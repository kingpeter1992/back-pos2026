package com.king.pos.Interface;

import java.util.List;

import com.king.pos.Dto.VenteRequest;
import com.king.pos.Dto.Response.VenteResponse;

public interface VenteService {
      VenteResponse enregistrerVente(VenteRequest request);
      List<VenteResponse> getAllVente();
      VenteResponse annulerVente(Long venteId);
}