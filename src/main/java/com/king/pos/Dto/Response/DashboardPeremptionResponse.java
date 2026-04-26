package com.king.pos.Dto.Response;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardPeremptionResponse {

    private long totalLots;
    private long totalLotsValides;
    private long totalLotsEnAlerte;
    private long totalLotsPerimes;
    private long totalExpireAujourdHui;
    private long totalAlerte7Jours;
    private long totalAlerte30Jours;
    private long totalAlerte170Jours;
    private long totalAlerte350Jours;
}