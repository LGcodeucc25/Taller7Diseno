-- Datos iniciales de SmartLibrary.
-- Los ids se generan por identidad en orden de inserción (1, 2, 3...).

-- Estudiantes (ids 1 y 2)
INSERT INTO estudiante (nombre, correo) VALUES ('Ana Gómez', 'ana.gomez@smartlibrary.edu');
INSERT INTO estudiante (nombre, correo) VALUES ('Carlos Ruiz', 'carlos.ruiz@smartlibrary.edu');

-- Libros: 1 y 2 disponibles, 3 no disponible (ya prestado)
INSERT INTO libro (titulo, disponible) VALUES ('Clean Code', TRUE);
INSERT INTO libro (titulo, disponible) VALUES ('Design Patterns', TRUE);
INSERT INTO libro (titulo, disponible) VALUES ('Refactoring', FALSE);

-- Préstamo previo activo: libro 3 prestado al estudiante 2
INSERT INTO prestamo (fecha_prestamo, fecha_devolucion, estudiante_id, libro_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), NULL, 2, 3);
