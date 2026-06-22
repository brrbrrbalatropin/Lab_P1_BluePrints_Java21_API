package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.persistence.entity.BlueprintEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para {@link BlueprintEntity}.
 * <p>
 * Spring genera las consultas a partir del nombre de los métodos.
 */
public interface BlueprintJpaRepository extends JpaRepository<BlueprintEntity, Long> {

    Optional<BlueprintEntity> findByAuthorAndName(String author, String name);

    List<BlueprintEntity> findByAuthor(String author);

    boolean existsByAuthorAndName(String author, String name);
}
