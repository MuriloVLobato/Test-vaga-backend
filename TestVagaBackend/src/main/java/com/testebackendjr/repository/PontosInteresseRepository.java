package com.testebackendjr.repository;

import com.testebackendjr.model.PontosInteresse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PontosInteresseRepository extends JpaRepository<PontosInteresse, Long> {

    @Query(
            """
            SELECT p FROM PontosInteresse p
            WHERE (p.x >= :xMin AND p.x <= :xMax AND (p.y >= :yMin AND p.y <= :yMax))
            """
    )

    List<PontosInteresse> findPontosInteresseProximos
            (
                @Param("xMin")    Long xMin,
                @Param("xMax")    Long xMax,
                @Param("yMin")    Long yMin,
                @Param("yMax")    Long yMax
            );
}
