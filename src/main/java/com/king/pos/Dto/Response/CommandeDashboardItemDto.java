package com.king.pos.Dto.Response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CommandeDashboardItemDto {
 private Long id;
    private String refCommande;
    private String fournisseurNom;
    private LocalDate dateCommande;
    private LocalDate datePrevue;
    private String statut;
    private BigDecimal montantTotal = BigDecimal.ZERO;
    private String devise;
    private BigDecimal quantiteTotale = BigDecimal.ZERO;
    private BigDecimal quantiteRecue = BigDecimal.ZERO;
    private BigDecimal progression = BigDecimal.ZERO;

    private Long joursRetard = 0L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRefCommande() {
        return refCommande;
    }

    public void setRefCommande(String refCommande) {
        this.refCommande = refCommande;
    }

    public String getFournisseurNom() {
        return fournisseurNom;
    }

    public void setFournisseurNom(String fournisseurNom) {
        this.fournisseurNom = fournisseurNom;
    }

    public LocalDate getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDate dateCommande) {
        this.dateCommande = dateCommande;
    }

    public LocalDate getDatePrevue() {
        return datePrevue;
    }

    public void setDatePrevue(LocalDate datePrevue) {
        this.datePrevue = datePrevue;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public BigDecimal getQuantiteTotale() {
        return quantiteTotale;
    }

    public void setQuantiteTotale(BigDecimal quantiteTotale) {
        this.quantiteTotale = quantiteTotale;
    }

    public BigDecimal getQuantiteRecue() {
        return quantiteRecue;
    }

    public void setQuantiteRecue(BigDecimal quantiteRecue) {
        this.quantiteRecue = quantiteRecue;
    }

    public BigDecimal getProgression() {
        return progression;
    }

    public void setProgression(BigDecimal progression) {
        this.progression = progression;
    }

    public Long getJoursRetard() {
        return joursRetard;
    }

    public void setJoursRetard(Long joursRetard) {
        this.joursRetard = joursRetard;
    }
}