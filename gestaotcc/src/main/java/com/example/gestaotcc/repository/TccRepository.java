package com.example.gestaotcc.repository;

import com.example.gestaotcc.entity.TccEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TccRepository extends JpaRepository<TccEntity, Long> {
}
