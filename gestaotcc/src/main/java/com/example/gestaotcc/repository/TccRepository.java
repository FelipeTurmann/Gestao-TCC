package com.example.gestaotcc.repository;

import com.example.gestaotcc.model.TccEntity;
import com.example.gestaotcc.model.UsuarioEntity;
import com.example.gestaotcc.model.enuns.StatusTcc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TccRepository extends JpaRepository<TccEntity, Long> {

    List<TccEntity> findByAluno(UsuarioEntity aluno);

    List<TccEntity> findByOrientador(UsuarioEntity orientador);

    List<TccEntity> findByStatus(StatusTcc status);

    List<TccEntity> findByAlunoAndStatus(UsuarioEntity aluno, StatusTcc status);

    List<TccEntity> findByOrientadorAndStatus(UsuarioEntity orientador, StatusTcc status);
}