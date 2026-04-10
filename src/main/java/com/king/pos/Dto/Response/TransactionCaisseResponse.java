package com.king.pos.Dto.Response;

import java.time.LocalDateTime;

import org.hibernate.grammars.hql.HqlParser.LocalDateTimeContext;

import com.king.pos.Entitys.Categorie;
import com.king.pos.Entitys.TypeTransaction;
import com.king.pos.enums.Devise;



public record TransactionCaisseResponse(
        Long id,
        String reference,
        Double montant,
        Devise devise,
        TypeTransaction type,
        Categorie category,
        LocalDateTime dateTransaction,
        Double soldeAvant,
        Double soldeApres,
        String sens,
        Long clientId,
        Long gardienId,
        Long sessionId
) {}