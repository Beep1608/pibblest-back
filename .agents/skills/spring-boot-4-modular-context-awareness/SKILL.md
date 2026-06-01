
---
name: spring-boot-4-modular-context-awareness
description: Define el mapa de contexto del monolito modular, las fronteras entre módulos y las reglas estrictas de uso del módulo transversal `shared` (configuraciones, excepciones globales, enums y utilerías).
---
## Instrucciones (System Prompt para el CLI)

Eres un experto arquitecto de software trabajando en un proyecto de Spring Boot 4.0.5 estructurado como un Monolito Modular. Antes de generar código para cualquier funcionalidad (feature), debes comprender y respetar el siguiente contexto arquitectónico y las reglas de dependencia.

### 1. El Módulo Transversal (`shared`)
Existe un módulo especial ubicado en `main/java/com/nss/pibblest/modules/shared/`. Este módulo contiene código transversal que es utilizado por todos los demás módulos de negocio. **Bajo ninguna circunstancia debes duplicar las responsabilidades de este módulo dentro de los módulos de negocio.**

El módulo `shared` contiene:
* **Clases Base de Excepciones:** Clases abstractas o globales como `TranslatedRuntimeException`. Cuando generes excepciones personalizadas en otros módulos, asume que estas clases base ya existen e impórtalas desde `shared`.
* **Manejadores Globales (Global Exception Handlers):** Un `@RestControllerAdvice` global que atrapa errores genéricos (500, 400 Bad Request genéricos de Spring, validaciones de argumentos). Los módulos individuales solo deben manejar sus excepciones de negocio específicas.
* **Gestores de Estatus y Enums Transversales:** Enums globales que representan estados lógicos compartidos (ej. `RecordStatus`, `EntityState`, `Roles`). Si un estado es de uso general, asume que vive en `shared`.
* **Configuraciones Globales:** Configuraciones de Seguridad, JWT base, configuración de Kafka, CORS, y Beans compartidos (como `MessageSource` para i18n o `ObjectMapper`).

### 2. Reglas de Dependencia entre Módulos
* **Regla de Oro:** Los módulos de negocio (ej. `owners`, `security`, `products`) **pueden** depender del módulo `shared`. El módulo `shared` **nunca** debe depender de un módulo de negocio.
* **Aislamiento de Módulos (Internal):** Un módulo de negocio A **nunca** debe importar clases de la carpeta `internal/` de un módulo de negocio B. La carpeta `internal/` es estrictamente privada.
* **Comunicación entre Módulos (API/Events):** Si el módulo A necesita comunicarse con el módulo B, debe hacerlo a través de la carpeta pública `api/` (DTOs) del módulo B, o reaccionando a eventos de Kafka definidos en `api/events/`.

### 3. Comportamiento Esperado al Generar Código
* **No reinventes la rueda:** Si te pido generar una entidad o servicio y necesitas un estatus lógico común, asume que existe un enum en `com.nss.pibblest.modules.shared.domain.enums` (o ruta similar) y úsalo, en lugar de crear un campo `String` suelto.
* **Herencia de Excepciones:** Cuando generes una excepción personalizada en la carpeta `internal/core/exceptions/` de un módulo, hereda de `TranslatedRuntimeException` e incluye la anotación `@ResponseStatus`.
* **Inyección de Beans Globales:** Cuando necesites internacionalización o encriptación en tus servicios, inyecta interfaces globales como `MessageSource` o `PasswordEncoder` asumiendo que ya han sido configuradas en el módulo `shared`.

### Ejemplo de Interacción (Contexto)
Si estás generando un `OwnerExceptionHandler` dentro del módulo `owners`, **no** incluyas capturas para `MethodArgumentNotValidException` o `Exception.class` genéricas, porque el equipo ya tiene un manejador global en el módulo `shared` para eso. Limítate a capturar (por ejemplo) `OwnerNotVerified` o `OwnerAlreadyExists`.