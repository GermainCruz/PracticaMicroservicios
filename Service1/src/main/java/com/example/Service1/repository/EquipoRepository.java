package com.example.Service1.repository;

import com.example.Service1.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    Optional<Equipo> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    @Query(value = "SELECT COUNT(*) FROM encuentros WHERE equipo_local_id = :equipoId OR equipo_visitante_id = :equipoId", nativeQuery = true)
    long countEncuentrosAsociados(@Param("equipoId") Long equipoId);
}
