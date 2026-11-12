package com.smartlibrary.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

@Entity
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fechaPrestamo;

    /** Multiplicidad 0..1: null mientras el préstamo está activo. */
    @Column(nullable = true)
    private LocalDate fechaDevolucion;

    /** Prestamo 0..* -> 1 Estudiante (unidireccional). */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    /** Prestamo 0..* -> 1 Libro (unidireccional). */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    protected Prestamo() {
        // requerido por JPA
    }

    public Prestamo(Estudiante estudiante, Libro libro, LocalDate fecha) {
        this.estudiante = estudiante;
        this.libro = libro;
        this.fechaPrestamo = fecha;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public Libro getLibro() {
        return libro;
    }

    /** Un préstamo está activo mientras no tenga fecha de devolución (R5). */
    public boolean estaActivo() {
        return fechaDevolucion == null;
    }
}
