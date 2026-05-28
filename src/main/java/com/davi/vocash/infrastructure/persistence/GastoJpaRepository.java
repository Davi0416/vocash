package com.davi.vocash.infrastructure.persistence;

import com.davi.vocash.domain.model.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;

interface GastoJpaRepository extends JpaRepository<Gasto, Long> {
    java.util.List<Gasto> findByCategoria(String categoria);
}
