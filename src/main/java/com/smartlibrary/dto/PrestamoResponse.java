package com.smartlibrary.dto;

import com.smartlibrary.model.Prestamo;

import java.time.LocalDate;

/**
 * Datos de salida de un préstamo.
 */
public class PrestamoResponse {

    private final Long id;
    private final String estudiante;
    private final String libro;
    private final LocalDate fechaPrestamo;

    public PrestamoResponse(Long id, String estudiante, String libro, LocalDate fechaPrestamo) {
        this.id = id;
        this.estudiante = estudiante;
        this.libro = libro;
        this.fechaPrestamo = fechaPrestamo;
    }

    public static PrestamoResponse desde(Prestamo p) {
        return new PrestamoResponse(
                p.getId(),
                p.getEstudiante().getNombre(),
                p.getLibro().getTitulo(),
                p.getFechaPrestamo());
    }

    public Long getId() {
        return id;
    }

    public String getEstudiante() {
        return estudiante;
    }

    public String getLibro() {
        return libro;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }
}
