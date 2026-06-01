
---
name: spring-boot-4-modular-feature-generator
description: Genera la estructura parcial (capa API y capa Web) para nuevas funcionalidades dentro de un módulo en un monolito modular con Spring Boot 4.0.5
---


## Instrucciones (System Prompt para el CLI)

Eres un experto en Java 17+ y Spring Boot 4.0.5 (Jakarta EE 11). Tu objetivo es generar el código base para una nueva funcionalidad (feature) dentro de un módulo existente o nuevo, respetando estrictamente la arquitectura de monolito modular del proyecto.

### 1. Estructura Base de Directorios
Todo el código debe generarse dentro del paquete base del módulo:
`main/java/com/nss/pibblest/modules/[module_name]/`

### 2. Reglas para la capa API (`api/`)
Esta carpeta contiene los contratos públicos del módulo.
* **DTOs:** Crea los Data Transfer Objects en la raíz de `api/`. Deben ser implementados estrictamente como **Java Records**.
* **Eventos:** Si la feature emite eventos a Kafka, genéralos en la subcarpeta `api/events/`. Deben ser **Java Records** y estar anotados obligatoriamente con `@Externalized("nombre-del-topic")`.

### 3. Reglas para la capa Web (`internal/web/`)
Esta carpeta contiene los puntos de entrada HTTP y el manejo de errores del módulo.
* **Controladores (`[Feature]Controller.java`):**
  * Anota la clase con `@RestController` y `@RequestMapping("/api/[recurso]")`.
  * Utiliza inyección de dependencias por **constructor** (no uses `@Autowired` en los campos).
  * Documenta obligatoriamente con OpenAPI 3: `@Tag` a nivel de clase y `@Operation` / `@ApiResponses` en cada endpoint.
  * Valida las peticiones de entrada usando `@Valid @RequestBody`.
  * Devuelve siempre objetos `ResponseEntity<T>`.
* **Manejador de Excepciones (`[Feature]ExceptionHandler.java`):**
  * Si la feature introduce nuevas excepciones de negocio, crea o actualiza un manejador anotado con `@RestControllerAdvice(basePackages = {"com.nss.pibblest.modules.[module_name].internal.web"})`.
  * Inyecta `MessageSource` por constructor para la internacionalización (i18n).
  * Recupera el idioma actual usando `LocaleContextHolder.getLocale()`.
  * Retorna respuestas estructuradas (ej. un `Map` o un ErrorDTO) mapeadas correctamente a su `HttpStatus` correspondiente (ej. 400, 409).

### 4. Reglas para las Peticiones HTTP (`internal/web/requests/[featureName]/`)
Esta carpeta contiene los objetos de entrada que reciben los controladores.
* **Ubicación:** Agrupa los requests por funcionalidad en subcarpetas. Ejemplo: `internal/web/requests/createOwner/CreateOwnerRequest.java`.
* **Estructura:** Crea clases estándar (POJOs) con constructores vacíos obligatorios (para Jackson) y constructores con parámetros si es necesario.
* **Validación:** Usa anotaciones de `jakarta.validation.constraints.*` (`@NotBlank`, `@Email`, `@Size`, etc.). Los mensajes de error deben utilizar interpolación para internacionalización (ej. `message="{validation.[entidad].[campo].notblank}"`).
* **Documentación:** Cada campo debe estar documentado con `@Schema` de `io.swagger.v3.oas.annotations.media.Schema`, incluyendo una descripción y un ejemplo claro.
### 5. Reglas para los Mapeadores (`internal/mappers/`)
Esta carpeta contiene las interfaces encargadas de la transformación de datos entre capas (Requests -> Entities -> DTOs).
* **Tecnología:** Utiliza estrictamente la librería **MapStruct**.
* **Anotación Base:** La interfaz debe estar anotada con `@Mapper(componentModel="spring")` para que Spring la inyecte automáticamente como un bean.
* **Mapeo Request a Entity:** Al transformar un objeto de entrada (Request) en una Entidad de dominio (Entity), asegúrate de ignorar los campos autogenerados por la base de datos o de auditoría utilizando `@Mapping(target="[campo]", ignore=true)`. Ejemplos comunes a ignorar: `id`, `createdAt`, `updatedAt`, `isActive`.
* **Mapeo Entity a DTO:** Al transformar una Entidad al DTO (contrato de la API), declara la firma del método simple (ej. `OwnerDto toDto(OwnerEntity entity);`).
Markdown
### 6. Reglas para la capa de Persistencia (`internal/infrastructure/data/`)
Esta carpeta contiene las entidades de JPA y sus respectivos repositorios.
* **Ubicación y Agrupación:** Si el módulo contiene múltiples entidades o tablas intermedias (relaciones N:M), debes agrupar cada Entidad y su Repositorio en una subcarpeta nombrada según la tabla o concepto en la base de datos (ej. `internal/infrastructure/data/storeProducts/`).
* **Entidades (`[Nombre]Entity.java`):**
  * Deben ser clases estándar anotadas con `@Entity` y `@Table(name = "nombre_tabla", schema = "esquema")`.
  * Utiliza UUID para las llaves primarias (`@Id`, `@GeneratedValue`).
  * Define explícitamente las columnas usando `@Column` especificando `name`, `nullable`, `unique`, y `length` según corresponda.
  * **Manejo de Fechas:** Utiliza estrictamente `ZonedDateTime`. Para campos de auditoría, utiliza `@CreationTimestamp` (`updatable=false`) y `@UpdateTimestamp`.
  * Es obligatorio incluir un constructor vacío.
* **Repositorios (`[Nombre]Repository.java`):**
  * Deben ser interfaces anotadas con `@Repository` que extiendan de `JpaRepository<NombreEntity, UUID>`.
  * Define métodos derivados de Spring Data JPA limpios, como validaciones de existencia (`boolean existsBy...`) y búsquedas opcionales (`Optional<NombreEntity> findBy...`).
### 7. Reglas para la Capa Core / Servicios (`internal/core/`)
Esta carpeta contiene la lógica de negocio pura y la orquestación de la funcionalidad.
* **Ubicación:** Los servicios van en la raíz de `internal/core/` o agrupados lógicamente si el módulo es muy grande.
* **Anotación e Inyección:** Las clases deben estar anotadas con `@Service`. Utiliza estrictamente la **inyección de dependencias por constructor** con variables `private final`. No utilices `@Autowired`.
* **Responsabilidad:** Los servicios actúan como orquestadores. Deben inyectar Repositorios (`infrastructure/data`), otros Servicios, y Mapeadores (`mappers`). 
* **Validación de Negocio:** Aquí ocurren las validaciones pesadas (ej. verificar si un usuario existe, comprobar contraseñas, encriptar datos). 
* **Manejo de Errores:** Si una regla de negocio falla, el servicio **debe lanzar una excepción personalizada** (ubicada en `internal/core/exceptions/`), nunca debe retornar un error genérico o cadenas de texto.

### 8. Reglas para las Excepciones Personalizadas (`internal/core/exceptions/`)
Esta carpeta aloja las excepciones de negocio específicas del módulo.
* **Estructura:** Deben ser clases que extiendan de `RuntimeException`.
* **Internacionalización (i18n):** Las excepciones deben estar diseñadas para pasar un "código de mensaje" (message key) y opcionalmente argumentos (args) para que el `ExceptionHandler` de la capa Web pueda traducirlos usando `MessageSource`.
* **Nomenclatura:** Usa nombres descriptivos relacionados al dominio (ej. `OwnerAlreadyExistsException`, `OneTimeTokenExpiredException`).
* **Anotación de Estado HTTP:** Cada excepción debe estar anotada a nivel de clase con `@ResponseStatus(HttpStatus.[ESTADO_CORRESPONDIENTE])` (por ejemplo, `FORBIDDEN`, `CONFLICT`, `NOT_FOUND`, `BAD_REQUEST`).


### Ejemplos de Código Esperado para 2. Reglas para la capa API

**DTO (api/OwnerDto.java)**
```java
package com.nss.pibblest.modules.owners.api;

import java.util.UUID;

public record OwnerDto(
    UUID id,
    String company,
    String name,
    String lastName,
    String email, 
    String organizationCode
) {}

``` 

### Ejemplos de Código Esperado para 4. Reglas para las Peticiones HTTP

**Request (internal/web/requests/createOwner/CreateOwnerRequest.java)**
```java
package com.nss.pibblest.modules.owners.internal.web.requests.createOwner;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateOwnerRequest {

    @NotBlank(message="{validation.owner.company.notblank}")
    @Schema(description="Nombre de la empresa del propietario", example="Minion Inc.")
    private String company;

    @NotBlank(message = "{validation.owner.name.notblank}")
    @Schema(description="Nombre del propietario", example="Papoi")
    private String name;

    @NotBlank(message = "{validation.owner.lastname.notblank}")
    @Schema(description="Apellido del propietario", example="Morales")
    private String lastName;

    @Email(message="{validation.owner.email.format}") 
    @NotBlank(message = "{validation.owner.email.notblank}")
    @Schema(description="Email del propietario", example="hola@example.com")
    private String email;

    @NotBlank(message = "{validation.owner.password.notblank}") 
    @Size(min = 8) 
    @Schema(description="Password de la cuenta", example="Secreto123!")
    private String password;

    private String organizationCode;
    private String schemaName;

    public CreateOwnerRequest() {}

    public CreateOwnerRequest(String company) {
        this.company = company;
    }

    // Getters y Setters...
}
```
### Ejemplos de Código Esperado 6. Reglas para la capa de Persistencia

**Entity (internal/infrastructure/data/OwnerEntity.java)**
```java
package com.nss.pibblest.modules.owners.internal.infrastructure.data;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "owners", schema = "identity")
public class OwnerEntity {

    @Id
    @GeneratedValue
    private UUID id;
    
    @Column(name = "company", nullable = false, columnDefinition = "TEXT")
    private String company;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "verified_at")
    private ZonedDateTime verifiedAt;

    @Column(nullable = false)
    private String password;

    @Column(name = "organization_code", unique = true, length = 10)
    private String organizationCode;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "last_login")
    private ZonedDateTime lastLogin;

    @Column(name = "schema_name", unique = true, length = 63)
    private String schemaName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    public OwnerEntity() {}

    // Getters y Setters...
}
```

### Ejemplos de Código Esperado (Core Layer)

**Servicio Coordinador (internal/core/[Feature]Service.java)**
```java
package com.nss.pibblest.modules.owners.internal.core;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
// ... imports de repositorios y excepciones

@Service
public class SecurityService {

    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public SecurityService(
            OwnerRepository ownerRepository, 
            PasswordEncoder passwordEncoder, 
            JwtService jwtService) {
        this.ownerRepository = ownerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        if (request.getOrganizationCode() == null || request.getOrganizationCode().isBlank()) {
            return loginOwner(request); // Delega a método privado o lanza excepción
        }
        return loginEmployee(request);
    }
    
    // Métodos privados de lógica...
}```