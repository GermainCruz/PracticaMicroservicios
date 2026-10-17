package com.example.Service1.service;

import com.example.Service1.dto.EquipoDto;
import com.example.Service1.model.Equipo;
import com.example.Service1.repository.EquipoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;

    public EquipoService(EquipoRepository equipoRepository) {
        this.equipoRepository = equipoRepository;
    }

    @Transactional(readOnly = true)
    public List<Equipo> findAll() {
        return equipoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Equipo> findById(Long id) {
        return equipoRepository.findById(id);
    }

    @Transactional
    public Equipo create(EquipoDto dto) {
        if (equipoRepository.existsByNombreIgnoreCase(dto.getNombre().trim())) {
            throw new IllegalArgumentException("Ya existe un equipo con el nombre: " + dto.getNombre().trim());
        }
        Equipo equipo = new Equipo();
        equipo.setNombre(dto.getNombre().trim());
        equipo.setCiudad(dto.getCiudad().trim());
        equipo.setEstadio(dto.getEstadio() != null ? dto.getEstadio().trim() : null);
        return equipoRepository.save(equipo);
    }

    @Transactional
    public Optional<Equipo> update(Long id, EquipoDto dto) {
        return equipoRepository.findById(id).map(equipo -> {
            if (equipoRepository.existsByNombreIgnoreCaseAndIdNot(dto.getNombre().trim(), id)) {
                throw new IllegalArgumentException("Ya existe otro equipo con el nombre: " + dto.getNombre().trim());
            }
            equipo.setNombre(dto.getNombre().trim());
            equipo.setCiudad(dto.getCiudad().trim());
            equipo.setEstadio(dto.getEstadio() != null ? dto.getEstadio().trim() : null);
            return equipoRepository.save(equipo);
        });
    }

    @Transactional
    public void delete(Long id) {
        if (!equipoRepository.existsById(id)) {
            throw new IllegalArgumentException("Equipo no encontrado con ID: " + id);
        }
        long encuentrosCount = equipoRepository.countEncuentrosAsociados(id);
        if (encuentrosCount > 0) {
            throw new IllegalStateException("No se puede eliminar el equipo (ID: " + id + ") porque tiene " + encuentrosCount + " encuentro(s) asociado(s) en la tabla 'encuentros'.");
        }
        equipoRepository.deleteById(id);
    }
}
