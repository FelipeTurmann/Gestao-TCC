package com.example.gestaotcc.repository;

import com.example.gestaotcc.model.EntregaEntity;
import com.example.gestaotcc.model.TccEntity;
import com.example.gestaotcc.model.enuns.TipoEntrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntregaRepository extends JpaRepository<EntregaEntity, Long> {

    List<EntregaEntity> findByTcc(TccEntity tcc);

    List<EntregaEntity> findByTccAndTipo(TccEntity tcc, TipoEntrega tipo);

    Optional<EntregaEntity> findFirstByTccOrderByDataEnvioDesc(TccEntity tcc);
}