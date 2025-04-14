package com.example.gestaotcc.repository;

import com.example.gestaotcc.model.AvaliacaoEntity;
import com.example.gestaotcc.model.EntregaEntity;
import com.example.gestaotcc.model.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<AvaliacaoEntity, Long> {

    List<AvaliacaoEntity> findByEntrega(EntregaEntity entrega);

    List<AvaliacaoEntity> findByOrientador(UsuarioEntity orientador);

    Optional<AvaliacaoEntity> findByEntregaAndOrientador(EntregaEntity entrega, UsuarioEntity orientador);
}