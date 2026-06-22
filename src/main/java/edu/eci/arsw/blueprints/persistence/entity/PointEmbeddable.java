package edu.eci.arsw.blueprints.persistence.entity;

import jakarta.persistence.Embeddable;

/**
 * Representación JPA de un punto, embebida dentro de {@link BlueprintEntity}.
 * <p>
 * JPA requiere una clase mutable con constructor sin argumentos, por eso no se
 * reutiliza el record de dominio {@code Point}.
 */
@Embeddable
public class PointEmbeddable {

    private int x;
    private int y;

    protected PointEmbeddable() { }

    public PointEmbeddable(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}
