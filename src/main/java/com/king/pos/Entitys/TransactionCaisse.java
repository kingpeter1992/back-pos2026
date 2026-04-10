package com.king.pos.Entitys;

import java.time.LocalDateTime;

import com.king.pos.enums.Devise;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class TransactionCaisse {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private TypeTransaction type; // ENCAISSEMENT, DECAISSEMENT

  @Enumerated(EnumType.STRING)
  private Devise devise; // USD, CDF
  private double montant;
@ManyToOne
@JoinColumn(name = "categorie_id")
private Categorie categorie;


  @Enumerated(EnumType.STRING)
  private ModePaiement modePaiement;

  private String description;
  private String reference;

  private double soldeAvant;

  private double soldeApres;

  private String sens; // + / -
  private String userId;

  private LocalDateTime dateTransaction;

  @ManyToOne(optional = false)
  @JoinColumn(name="session_id")
  private CaisseSession session;

  @ManyToOne @JoinColumn(name="client_id")
  private Client client;


  @ManyToOne
@JoinColumn(name="caisse_id")
private Caisse caisse;
}
