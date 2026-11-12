# SmartLibrary – Registrar préstamo

API REST con Spring Boot que implementa el caso de uso **Registrar préstamo** de la biblioteca SmartLibrary. Un estudiante solicita un libro y el sistema registra el préstamo si el libro está disponible. El código sigue el diseño UML del taller (clases, atributos, operaciones y paquetes) para que se pueda verificar la trazabilidad entre el diagrama y el código.

## Reglas de negocio

| Regla | Descripción |
|-------|-------------|
| R1 | Solo un libro disponible puede prestarse. |
| R2 | El préstamo registra estudiante, libro y fecha. |
| R3 | Al prestarlo, el libro deja de estar disponible. |
| R4 | Una solicitud inválida no debe crear préstamo. |
| R5 | Un libro puede tener varios préstamos históricos, pero solo uno activo a la vez. |

## Tecnologías

- Java 21
- Spring Boot 3.5.6 (Spring Web, Spring Data JPA, Validation)
- H2 Database (en memoria)
- Maven (incluye Maven Wrapper: `mvnw` / `mvnw.cmd`)
- JUnit 5 + MockMvc para las pruebas de integración

## Estructura de paquetes

```
src/main/java/com/smartlibrary
├── SmartLibraryApplication.java
├── controller
│   └── PrestamoController.java        # Expone /api/prestamos (solo usa PrestamoService)
├── service
│   └── PrestamoService.java           # registrarPrestamo(), listarPrestamos()
├── repository
│   ├── EstudianteRepository.java
│   ├── LibroRepository.java
│   └── PrestamoRepository.java
├── model
│   ├── Estudiante.java
│   ├── Libro.java                     # marcarPrestado() (Experto GRASP)
│   └── Prestamo.java                  # estaActivo()
├── dto
│   ├── PrestamoRequest.java
│   └── PrestamoResponse.java          # desde(Prestamo)
└── exception
    ├── RecursoNoEncontradoException.java
    ├── LibroNoDisponibleException.java
    └── ManejadorErrores.java          # 404 / 409 / 400

src/main/resources
├── application.properties
└── data.sql                           # Datos iniciales

src/test/java/com/smartlibrary/controller
└── PrestamoControllerIntegrationTest.java

postman/SmartLibrary.postman_collection.json
```

### Relaciones del modelo

- `Prestamo` 0..* → 1 `Estudiante` (unidireccional, `@ManyToOne` obligatorio)
- `Prestamo` 0..* → 1 `Libro` (unidireccional, `@ManyToOne` obligatorio)
- `Prestamo.fechaDevolucion` es opcional (0..1): mientras sea `null`, el préstamo está activo.

## Cómo ejecutar

Requisitos: JDK 21. Maven es opcional, porque el proyecto incluye el Maven Wrapper.

```bash
# Con Maven instalado
mvn spring-boot:run

# Con el wrapper (Windows)
mvnw.cmd spring-boot:run

# Con el wrapper (Linux / macOS)
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

**Consola H2:** `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:smartlibrary`
- Usuario: `sa` · Contraseña: (vacía)

**Ejecutar las pruebas:**

```bash
mvn test        # o mvnw.cmd test / ./mvnw test
```

## Datos iniciales

| Tabla | Datos |
|-------|-------|
| Estudiantes | 1 – Ana Gómez · 2 – Carlos Ruiz |
| Libros | 1 – Clean Code (disponible) · 2 – Design Patterns (disponible) · 3 – Refactoring (**no disponible**) |
| Préstamos | 1 – libro 3 prestado al estudiante 2 (activo) |

## Endpoints

| Método | Ruta | Descripción | Respuestas |
|--------|------|-------------|------------|
| POST | `/api/prestamos` | Registra un préstamo | 201 Created (+ header `Location`), 400, 404, 409 |
| GET | `/api/prestamos` | Lista todos los préstamos | 200 OK |

**Cuerpo de la petición (POST):**

```json
{ "estudianteId": 1, "libroId": 1 }
```

**Respuesta exitosa (201):**

```json
{ "id": 2, "estudiante": "Ana Gómez", "libro": "Clean Code", "fechaPrestamo": "2026-11-12" }
```

**Formato de error:**

```json
{ "estado": 409, "error": "Conflict", "mensaje": "El libro no está disponible: 3" }
```

## Escenarios de prueba

Los ejemplos suponen la aplicación recién iniciada (datos iniciales). En la carpeta [`postman/`](postman/) está la colección `SmartLibrary.postman_collection.json` (formato v2.1) con estas 6 peticiones y sus aserciones; impórtela en Postman y ejecútela en orden.

### 1. Préstamo de libro disponible → 201 Created

```bash
curl -i -X POST http://localhost:8080/api/prestamos \
  -H "Content-Type: application/json" \
  -d '{"estudianteId": 1, "libroId": 1}'
```

Resultado: `201 Created`, header `Location: /api/prestamos/2`, el préstamo queda registrado y el libro 1 pasa a no disponible.

### 2. Libro ya prestado → 409 Conflict

```bash
curl -i -X POST http://localhost:8080/api/prestamos \
  -H "Content-Type: application/json" \
  -d '{"estudianteId": 1, "libroId": 3}'
```

Resultado: `409 Conflict`, `"mensaje": "El libro no está disponible: 3"`. No se crea ningún préstamo.

### 3. Estudiante inexistente → 404 Not Found

```bash
curl -i -X POST http://localhost:8080/api/prestamos \
  -H "Content-Type: application/json" \
  -d '{"estudianteId": 999, "libroId": 2}'
```

Resultado: `404 Not Found`, `"mensaje": "Estudiante no encontrado: 999"`.

### 4. Libro inexistente → 404 Not Found

```bash
curl -i -X POST http://localhost:8080/api/prestamos \
  -H "Content-Type: application/json" \
  -d '{"estudianteId": 1, "libroId": 999}'
```

Resultado: `404 Not Found`, `"mensaje": "Libro no encontrado: 999"`.

### 5. Consulta de préstamos → 200 OK

```bash
curl -i http://localhost:8080/api/prestamos
```

Resultado: `200 OK` con la lista de préstamos (al menos el préstamo inicial del libro 3).

### Extra: solicitud inválida → 400 Bad Request

```bash
curl -i -X POST http://localhost:8080/api/prestamos \
  -H "Content-Type: application/json" \
  -d '{"estudianteId": null, "libroId": 2}'
```

Resultado: `400 Bad Request`, `"mensaje": "estudianteId es obligatorio"`. No se crea ningún préstamo.

> En Windows PowerShell use `curl.exe` y escape las comillas del JSON, o use la colección de Postman.

## Flujo de `registrarPrestamo` (diagrama de secuencia)

```
PrestamoController.registrar(request)
  └─ PrestamoService.registrarPrestamo(request)          @Transactional
       1. estudianteRepository.findById(estudianteId)    → 404 si no existe
       2. libroRepository.findById(libroId)              → 404 si no existe
       3. libro.isDisponible()                           → 409 si es false
       4. new Prestamo(estudiante, libro, LocalDate.now())
       5. libro.marcarPrestado()
       6. libroRepository.save(libro)
       7. return prestamoRepository.save(prestamo)
```

## Trazabilidad Regla → Clase → Operación → Prueba

| Regla | Clase | Operación | Prueba (`PrestamoControllerIntegrationTest`) |
|-------|-------|-----------|----------------------------------------------|
| R1 – Solo un libro disponible puede prestarse | `PrestamoService`, `Libro`, `LibroNoDisponibleException`, `ManejadorErrores` | `registrarPrestamo()` (paso 3), `Libro.isDisponible()`, `manejarNoDisponible()` → 409 | `libroYaPrestado` (escenario 2) |
| R2 – El préstamo registra estudiante, libro y fecha | `Prestamo`, `PrestamoService`, `PrestamoResponse` | `new Prestamo(estudiante, libro, fecha)`, `registrarPrestamo()` (pasos 4 y 7), `PrestamoResponse.desde()` | `prestamoDeLibroDisponible` (escenario 1), `consultaDePrestamos` (escenario 5) |
| R3 – Al prestarlo, el libro deja de estar disponible | `Libro`, `PrestamoService` | `Libro.marcarPrestado()`, `registrarPrestamo()` (pasos 5 y 6) | `prestamoDeLibroDisponible` (verifica `isDisponible() == false`) |
| R4 – Una solicitud inválida no crea préstamo | `PrestamoRequest`, `PrestamoController`, `PrestamoService`, `RecursoNoEncontradoException`, `ManejadorErrores` | `@NotNull` + `@Valid` en `registrar()`, `registrarPrestamo()` (pasos 1 y 2, `@Transactional`), `manejarNoEncontrado()` → 404, manejo de `MethodArgumentNotValidException` → 400 | `estudianteInexistente` (escenario 3), `libroInexistente` (escenario 4), `solicitudInvalida` |
| R5 – Varios préstamos históricos, uno activo a la vez | `Prestamo`, `Libro`, `PrestamoService` | `Prestamo.estaActivo()` (`fechaDevolucion == null`), relación `Prestamo` 0..* → 1 `Libro`, validación de disponibilidad en `registrarPrestamo()` | `libroYaPrestado` (el libro 3 con préstamo activo no admite otro), `consultaDePrestamos` |

## Pruebas automatizadas

`PrestamoControllerIntegrationTest` usa `@SpringBootTest` + `MockMvc` sobre la base H2 real. Con `@DirtiesContext(AFTER_EACH_TEST_METHOD)` cada prueba parte de los datos iniciales, así que las pruebas son independientes entre sí.

| # | Prueba | Esperado |
|---|--------|----------|
| 1 | `prestamoDeLibroDisponible` | 201 + préstamo registrado + libro no disponible |
| 2 | `libroYaPrestado` | 409 + cantidad de préstamos sin cambio |
| 3 | `estudianteInexistente` | 404 |
| 4 | `libroInexistente` | 404 |
| 5 | `consultaDePrestamos` | 200 con registros |
| 6 | `solicitudInvalida` | 400 + cantidad de préstamos sin cambio |
