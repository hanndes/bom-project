package com.handederelii.bom_project.repositories;

import com.handederelii.bom_project.entity.Bom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BomRepository extends JpaRepository<Bom, String> {
    Optional<Bom> findByScopeAndActorIdAndBodyHash(String scope, Long actorId, String bodyHash);

}
