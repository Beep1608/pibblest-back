1. Seguridad y Validación de Datos
Útil para controladores (Controllers), DTOs y servicios expuestos al exterior.

"...vulnerabilidades de inyección (SQL Injection, HQL Injection) y sanitización inadecuada de inputs."

"...fugas de información sensible (exposición de contraseñas, tokens o PII en logs, trazas de error o respuestas de la API)."

"...vulnerabilidades de tipo IDOR (Insecure Direct Object Reference), asegurando que el usuario autenticado tiene permisos para acceder a los recursos solicitados."

"...validaciones de entrada faltantes o insuficientes en los DTOs (falta de anotaciones @Valid, @NotNull, @Size, validación de formatos)."



2. Lógica de Negocio y Transaccionalidad (Crítico para un POS)
Útil para la capa de Servicios (Services) y casos de uso donde hay dinero, inventario o estados involucrados.

"...condiciones de carrera (race conditions) o problemas de concurrencia, especialmente en la actualización de inventarios o balances de cuentas."

"...fallos en la transaccionalidad, asegurando que las anotaciones @Transactional están bien aplicadas y que los rollbacks ocurrirán correctamente en caso de excepciones."

"...casos borde (edge cases) matemáticos o lógicos no manejados (ej. divisiones por cero, cálculos de impuestos erróneos, montos negativos no controlados)."

"...inconsistencias en la máquina de estados de la entidad principal (ej. permitir pagar una orden que ya está cancelada)."


3. Rendimiento y Persistencia (JPA/Hibernate)

Útil para la capa de Repositorios (Repositories) y relaciones entre Entidades (Entities).

"...problemas de rendimiento N+1 queries en consultas JPA o cargas ansiosas (Eager fetching) innecesarias."

"...consultas ineficientes a la base de datos o falta de paginación en métodos que podrían retornar grandes volúmenes de datos."

"...fugas de memoria (memory leaks) o mal manejo de recursos (conexiones de base de datos o streams de archivos no cerrados adecuadamente)."

"...operaciones bloqueantes dentro de bucles o procesamiento síncrono que podría delegarse a eventos o colas asíncronas."



4. Calidad del Código (Clean Code) y Estructura
Útil para refactorizaciones generales o revisión de código legado.

"...violaciones a los principios SOLID, enfocándote en clases con demasiadas responsabilidades (God Classes) o alto acoplamiento."

"...manejo silencioso de errores, buscando bloques catch vacíos o el uso de excepciones genéricas (Exception o RuntimeException) en lugar de excepciones de dominio personalizadas."

"...malas prácticas del framework Spring Boot, como inyección de dependencias por campo (@Autowired en variables) en lugar de constructores, o mal uso de los scopes de los beans."

"...violaciones a los límites del monolito modular, verificando que este módulo no esté fuertemente acoplado o importando clases internas de otros módulos directamente."