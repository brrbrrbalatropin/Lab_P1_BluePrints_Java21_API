package edu.eci.arsw.blueprints.persistence.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que mapea un blueprint a la tabla {@code blueprints}.
 * <p>
 * Usa una clave primaria sustituta ({@code id}) y una restricción de unicidad
 * sobre {@code (author, name)} para reflejar la clave lógica del dominio.
 * Los puntos se almacenan en una tabla secundaria {@code blueprint_points}
 * conservando el orden de inserción mediante {@link OrderColumn}.
 */
@Entity
@Table(name = "blueprints",
        uniqueConstraints = @UniqueConstraint(name = "uk_author_name", columnNames = {"author", "name"}))
public class BlueprintEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "blueprint_points",
            joinColumns = @JoinColumn(name = "blueprint_id"))
    @OrderColumn(name = "point_index")
    private List<PointEmbeddable> points = new ArrayList<>();

    protected BlueprintEntity() { }

    public BlueprintEntity(String author, String name, List<PointEmbeddable> points) {
        this.author = author;
        this.name = name;
        if (points != null) this.points = points;
    }

    public Long getId() { return id; }
    public String getAuthor() { return author; }
    public String getName() { return name; }
    public List<PointEmbeddable> getPoints() { return points; }

    public void setAuthor(String author) { this.author = author; }
    public void setName(String name) { this.name = name; }
    public void setPoints(List<PointEmbeddable> points) { this.points = points; }
}
