package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.entity.BlueprintEntity;
import edu.eci.arsw.blueprints.persistence.entity.PointEmbeddable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación de {@link BlueprintPersistence} respaldada por PostgreSQL vía Spring Data JPA.
 * <p>
 * Activa solo bajo el perfil {@code postgres}; en su ausencia se usa
 * {@code InMemoryBlueprintPersistence}. Traduce entre las entidades JPA y el
 * modelo de dominio para no acoplar el dominio a la tecnología de persistencia.
 */
@Repository
@Profile("postgres")
@Transactional
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    private final BlueprintJpaRepository repo;

    public PostgresBlueprintPersistence(BlueprintJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        if (repo.existsByAuthorAndName(bp.getAuthor(), bp.getName())) {
            throw new BlueprintPersistenceException(
                    "Blueprint already exists: " + bp.getAuthor() + ":" + bp.getName());
        }
        repo.save(toEntity(bp));
    }

    @Override
    @Transactional(readOnly = true)
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        BlueprintEntity e = repo.findByAuthorAndName(author, name)
                .orElseThrow(() -> new BlueprintNotFoundException(
                        "Blueprint not found: %s/%s".formatted(author, name)));
        return toDomain(e);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        List<BlueprintEntity> list = repo.findByAuthor(author);
        if (list.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints for author: " + author);
        }
        return list.stream().map(PostgresBlueprintPersistence::toDomain).collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Blueprint> getAllBlueprints() {
        return repo.findAll().stream()
                .map(PostgresBlueprintPersistence::toDomain)
                .collect(Collectors.toSet());
    }

    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        BlueprintEntity e = repo.findByAuthorAndName(author, name)
                .orElseThrow(() -> new BlueprintNotFoundException(
                        "Blueprint not found: %s/%s".formatted(author, name)));
        e.getPoints().add(new PointEmbeddable(x, y));
        repo.save(e);
    }

    // ---- mapeo entidad <-> dominio ----

    private static BlueprintEntity toEntity(Blueprint bp) {
        List<PointEmbeddable> pts = bp.getPoints().stream()
                .map(p -> new PointEmbeddable(p.x(), p.y()))
                .collect(Collectors.toList());
        return new BlueprintEntity(bp.getAuthor(), bp.getName(), pts);
    }

    private static Blueprint toDomain(BlueprintEntity e) {
        List<Point> pts = e.getPoints().stream()
                .map(p -> new Point(p.getX(), p.getY()))
                .collect(Collectors.toList());
        return new Blueprint(e.getAuthor(), e.getName(), pts);
    }
}
