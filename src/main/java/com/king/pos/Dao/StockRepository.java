package com.king.pos.Dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.king.pos.Dto.Response.StockAlerteView;
import com.king.pos.Dto.Response.StockProduitView;
import com.king.pos.Entitys.Depot;
import com.king.pos.Entitys.Produit;
import com.king.pos.Entitys.StockProduit;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<StockProduit, Long> {
   Optional<StockProduit> findByProduit(Produit produit);

    Optional<StockProduit> findByProduitId(Long produitId);

    Optional<StockProduit> findByProduitAndDepot(Produit produit, Depot depot);

    Optional<StockProduit> findByProduitIdAndDepotId(Long produitId, Long depotId);

    @Query("""
        select new com.king.pos.Dto.Response.StockProduitView(
            sp.id,
            p.id,
            p.nom,
            p.codeBarres,
            c.nom,
            d.id,
            d.nom,
            coalesce(sp.quantiteDisponible, 0),
            coalesce(sp.pmp, 0),
            coalesce(sp.valeurStock, 0),
            coalesce(p.stockMinimum, 0),
            coalesce(p.stockMaximum, 0),
            case
                when coalesce(sp.quantiteDisponible, 0) <= 0 then 'RUPTURE'
                when coalesce(p.stockMinimum, 0) > 0
                     and coalesce(sp.quantiteDisponible, 0) < coalesce(p.stockMinimum, 0) then 'ALERTE_RUPTURE'
                when coalesce(p.stockMaximum, 0) > 0
                     and coalesce(sp.quantiteDisponible, 0) > coalesce(p.stockMaximum, 0) then 'SURPLUS'
                else 'NORMAL'
            end,
            sp.dateDerniereEntree,
            sp.dateDerniereSortie,
            sp.dateCreation,
            sp.dateModification
        )
        from StockProduit sp
        join sp.produit p
        left join p.categorie c
        join sp.depot d
        order by p.nom asc, d.nom asc
    """)
    List<StockProduitView> findAllStockView();

    @Query("""
        select new com.king.pos.Dto.Response.StockAlerteView(
            sp.id,
            p.id,
            p.nom,
            p.codeBarres,
            c.nom,
            d.id,
            d.nom,
            coalesce(sp.quantiteDisponible, 0),
            coalesce(p.stockMinimum, 0),
            coalesce(p.stockMaximum, 0),
            case
                when coalesce(sp.quantiteDisponible, 0) <= 0 then 'RUPTURE'
                when coalesce(p.stockMinimum, 0) > 0
                     and coalesce(sp.quantiteDisponible, 0) < coalesce(p.stockMinimum, 0) then 'ALERTE_RUPTURE'
                when coalesce(p.stockMaximum, 0) > 0
                     and coalesce(sp.quantiteDisponible, 0) > coalesce(p.stockMaximum, 0) then 'SURPLUS'
                else 'NORMAL'
            end
        )
        from StockProduit sp
        join sp.produit p
        left join p.categorie c
        join sp.depot d
        where
            coalesce(sp.quantiteDisponible, 0) <= 0
            or (
                coalesce(p.stockMinimum, 0) > 0
                and coalesce(sp.quantiteDisponible, 0) < coalesce(p.stockMinimum, 0)
            )
            or (
                coalesce(p.stockMaximum, 0) > 0
                and coalesce(sp.quantiteDisponible, 0) > coalesce(p.stockMaximum, 0)
            )
        order by p.nom asc, d.nom asc
    """)
    List<StockAlerteView> findAlertesStock();



      @Query("""
        select coalesce(sum(sp.quantiteDisponible), 0)
        from StockProduit sp
        where sp.produit.id = :produitId
    """)
    BigDecimal getStockTotalByProduitId(Long produitId);

    @Query("""
        select sp.produit.id, coalesce(sum(sp.quantiteDisponible), 0), coalesce(avg(sp.pmp), 0)
        from StockProduit sp
        group by sp.produit.id
    """)
    List<Object[]> findStockAndPmpByProduit();
}