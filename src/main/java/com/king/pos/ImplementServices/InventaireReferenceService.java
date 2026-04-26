package com.king.pos.ImplementServices;


import com.king.pos.Dao.InventaireBordereauRepository;
import com.king.pos.Dao.InventaireRepository;
import com.king.pos.Entitys.Inventaire;
import com.king.pos.Entitys.InventaireBordereau;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class InventaireReferenceService {

    private final InventaireRepository inventaireRepository;
    private final InventaireBordereauRepository bordereauRepository;

    public String nextInventaireReference() {
        int next = inventaireRepository.findTopByOrderByIdDesc()
                .map(Inventaire::getId)
                .map(Long::intValue)
                .orElse(0) + 1;

        String ym = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        return "INV-" + ym + "-" + String.format("%04d", next);
    }

    public String nextBordereauReference() {
        int next = bordereauRepository.findTopByOrderByIdDesc()
                .map(InventaireBordereau::getId)
                .map(Long::intValue)
                .orElse(0) + 1;

        String ym = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        return "BDI-" + ym + "-" + String.format("%04d", next);
    }
}
