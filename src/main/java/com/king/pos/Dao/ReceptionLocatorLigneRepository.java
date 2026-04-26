package com.king.pos.Dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.king.pos.Entitys.ReceptionLocatorLigne;

public interface ReceptionLocatorLigneRepository extends JpaRepository<ReceptionLocatorLigne, Long> {

    List<ReceptionLocatorLigne> findByReceptionId(Long receptionId);
}
