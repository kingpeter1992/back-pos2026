package com.king.pos.Interface;

import java.util.List;

import com.king.pos.Dto.Response.ProvisionStockDashboardResponse;
import com.king.pos.Dto.Response.ProvisionStockResponse;

public interface ProvisionStockService {
        List<ProvisionStockResponse> calculerProvisionStock();

    ProvisionStockDashboardResponse getDashboardProvisionStock();
}
