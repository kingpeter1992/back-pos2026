package com.king.pos.Entitys;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class CaisseSession {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private LocalDate dateJour;

//  @Enumerated(EnumType.STRING)
  private String statut; // OUVERTE, FERMEE

  // Ouverture
  private double soldeInitialUSD = 0.0;

  private double soldeInitialCDF = 0.0;

  // Solde courant
  private double soldeActuelUSD =0.0;

  private double soldeActuelCDF =  0.0;

  // Clôture
  private LocalDateTime dateOuverture;
  private LocalDateTime dateCloture;

  private String openedBy;
  private String closedBy;

  private String noteOuverture;
  private String noteCloture;

  @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
  @JsonIgnore
private List<TransactionCaisse> transactions = new ArrayList<>();
}