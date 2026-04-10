package com.king.pos.Interfaces;

import java.time.LocalDateTime;

public interface TxDisponibleProjection {
     Long getId();
  LocalDateTime getDateTransaction();
  String getReference();
  double getMontant();
  double getDejaAffecte();
}
