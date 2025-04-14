package com.example.gestaotcc.repository;

import com.example.gestaotcc.model.UsuarioEntity;
import com.example.gestaotcc.model.enuns.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByEmail(String email);

    List<UsuarioEntity> findByTipoUsuario(TipoUsuario tipoUsuario);
}