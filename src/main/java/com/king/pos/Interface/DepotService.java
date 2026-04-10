package com.king.pos.Interface;


import java.util.List;

import com.king.pos.Dto.Response.DepotResponse;

public interface DepotService {
    List<DepotResponse> getAll();
    DepotResponse getById(Long id);
}
