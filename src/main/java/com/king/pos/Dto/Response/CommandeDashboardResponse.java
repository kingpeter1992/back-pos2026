package com.king.pos.Dto.Response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CommandeDashboardResponse {
     private long totalCommandes;
    private long totalBrouillon;
    private long totalEnCours;
    private long totalPartielLivre;
    private long totalLivre;
    private long totalAnnule;
    private long totalRetard;

    private BigDecimal montantTotal = BigDecimal.ZERO;
    private BigDecimal montantMoyen = BigDecimal.ZERO;

    private BigDecimal quantiteTotaleCommandee = BigDecimal.ZERO;
    private BigDecimal quantiteTotaleRecue = BigDecimal.ZERO;
    private BigDecimal tauxReceptionGlobal = BigDecimal.ZERO;

    private List<CommandeDashboardItemDto> commandesRecentes = new ArrayList<>();
    private List<CommandeDashboardItemDto> commandesEnRetard = new ArrayList<>();
    private List<FournisseurDashboardDto> topFournisseurs = new ArrayList<>();
    private List<String> alertes = new ArrayList<>();

    public long getTotalCommandes() {
        return totalCommandes;
    }

    public void setTotalCommandes(long totalCommandes) {
        this.totalCommandes = totalCommandes;
    }

    public long getTotalBrouillon() {
        return totalBrouillon;
    }

    public void setTotalBrouillon(long totalBrouillon) {
        this.totalBrouillon = totalBrouillon;
    }

    public long getTotalEnCours() {
        return totalEnCours;
    }

    public void setTotalEnCours(long totalEnCours) {
        this.totalEnCours = totalEnCours;
    }

    public long getTotalPartielLivre() {
        return totalPartielLivre;
    }

    public void setTotalPartielLivre(long totalPartielLivre) {
        this.totalPartielLivre = totalPartielLivre;
    }

    public long getTotalLivre() {
        return totalLivre;
    }

    public void setTotalLivre(long totalLivre) {
        this.totalLivre = totalLivre;
    }

    public long getTotalAnnule() {
        return totalAnnule;
    }

    public void setTotalAnnule(long totalAnnule) {
        this.totalAnnule = totalAnnule;
    }

    public long getTotalRetard() {
        return totalRetard;
    }

    public void setTotalRetard(long totalRetard) {
        this.totalRetard = totalRetard;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

    public BigDecimal getMontantMoyen() {
        return montantMoyen;
    }

    public void setMontantMoyen(BigDecimal montantMoyen) {
        this.montantMoyen = montantMoyen;
    }

    public BigDecimal getQuantiteTotaleCommandee() {
        return quantiteTotaleCommandee;
    }

    public void setQuantiteTotaleCommandee(BigDecimal quantiteTotaleCommandee) {
        this.quantiteTotaleCommandee = quantiteTotaleCommandee;
    }

    public BigDecimal getQuantiteTotaleRecue() {
        return quantiteTotaleRecue;
    }

    public void setQuantiteTotaleRecue(BigDecimal quantiteTotaleRecue) {
        this.quantiteTotaleRecue = quantiteTotaleRecue;
    }

    public BigDecimal getTauxReceptionGlobal() {
        return tauxReceptionGlobal;
    }

    public void setTauxReceptionGlobal(BigDecimal tauxReceptionGlobal) {
        this.tauxReceptionGlobal = tauxReceptionGlobal;
    }

    public List<CommandeDashboardItemDto> getCommandesRecentes() {
        return commandesRecentes;
    }

    public void setCommandesRecentes(List<CommandeDashboardItemDto> commandesRecentes) {
        this.commandesRecentes = commandesRecentes;
    }

    public List<CommandeDashboardItemDto> getCommandesEnRetard() {
        return commandesEnRetard;
    }

    public void setCommandesEnRetard(List<CommandeDashboardItemDto> commandesEnRetard) {
        this.commandesEnRetard = commandesEnRetard;
    }

    public List<FournisseurDashboardDto> getTopFournisseurs() {
        return topFournisseurs;
    }

    public void setTopFournisseurs(List<FournisseurDashboardDto> topFournisseurs) {
        this.topFournisseurs = topFournisseurs;
    }

    public List<String> getAlertes() {
        return alertes;
    }

    public void setAlertes(List<String> alertes) {
        this.alertes = alertes;
    }

}
