package com.alocanote.api.repository;

import com.alocanote.api.model.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    // Procura um local pelo nome
    Optional<Location> findByName(String name);

    // Procura um local especificamente pelo CEP
    Optional<Location> findByCep(String cep);
}
