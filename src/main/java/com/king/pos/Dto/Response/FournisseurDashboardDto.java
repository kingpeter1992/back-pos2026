package com.king.pos.Dto.Response;

import java.math.BigDecimal;

public class FournisseurDashboardDto {
    private Long fournisseurId;
    private String fournisseurNom;
    private long totalCommandes;
    private BigDecimal montantTotal = BigDecimal.ZERO;

    public Long getFournisseurId() {
        return fournisseurId;
    }

    public void setFournisseurId(Long fournisseurId) {
        this.fournisseurId = fournisseurId;
    }

    public String getFournisseurNom() {
        return fournisseurNom;
    }

    public void setFournisseurNom(String fournisseurNom) {
        this.fournisseurNom = fournisseurNom;
    }

    public long getTotalCommandes() {
        return totalCommandes;
    }

    public void setTotalCommandes(long totalCommandes) {
        this.totalCommandes = totalCommandes;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

}
