package com.smartlibrary.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Datos de entrada para registrar un préstamo.
 * Los getters son getEstudianteId() y getLibroId().
 */
public class PrestamoRequest {

    @NotNull(message = "estudianteId es obligatorio")
    private Long estudianteId;

    @NotNull(message = "libroId es obligatorio")
    private Long libroId;

    public PrestamoRequest() {
    }

    public PrestamoRequest(Long estudianteId, Long libroId) {
        this.estudianteId = estudianteId;
        this.libroId = libroId;
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public Long getLibroId() {
        return libroId;
    }
}
