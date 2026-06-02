package com.nss.pibblest.modules.notifications.internal.core.owner.integral;

import org.springframework.modulith.test.ApplicationModuleTest;
import org.testcontainers.junit.jupiter.Testcontainers;

@ApplicationModuleTest
@Testcontainers
public class OwnerSchemaProvisionerListenerIntegrationTest {

    //private static final Logger logger = LoggerFactory.getLogger(OwnerSchemaProvisionerListenerIntegrationTest.class);
//
    //@Container
    //
    //static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine")
    //    .withDatabaseName("pibblest_test_db")
    //    .withUsername("testuser")
    //    .withPassword("testpass");
//
    //@Autowired 
    //private OwnerSchemaProvisionerListener provisionerListener;
//
    //@Autowired
    //private JdbcTemplate jdbcTemplate;
    //
    //@DynamicPropertySource
    //static void configureProperties (DynamicPropertyRegistry registry) {
    //    postgres.start();
    //    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    //    registry.add("spring.datasource.username", postgres::getUsername);
    //    registry.add("spring.datasource.password", postgres::getPassword);
//
    //    registry.add("spring.liquibase.enabled", () -> "true");
    //    registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/global-master.yaml");
    //}
//
    //@Test
    //@DisplayName("Debe crear un esquema aislado y ejecutar liquibase para un nuevo Owner")
    //void debeAprovisionarEsquemayCrearTablasDelOwner(){
    //    String company = "Nihao";
    //    String email = "hola@example.com";
    //    String schemaName = "nihao_schema";
    //    OwnerRegisteredEvent event = new OwnerRegisteredEvent(UUID.randomUUID(), company, email, schemaName);
//
    //    assertDoesNotThrow(() -> provisionerListener.provisionNewOwnerDb(event), 
    //        "El método provisionNewOwnerDb lanzó una excepción inesperada.");
    //    
    //    // CORRECCIÓN 2: Consultar 'schemata' para verificar que el esquema existe
    //    Integer schemaExists = jdbcTemplate.queryForObject(
    //        "SELECT count(*) FROM information_schema.schemata WHERE schema_name = ?",
    //        Integer.class, schemaName);
    //    assertThat(schemaExists).isEqualTo(1)
    //        .withFailMessage("El esquema '%s' no se creó en la base de datos.", schemaName);
//
//
    //    // CORRECCIÓN 3: Usar 'table_schema' en lugar de 'schema_name'
    //    Integer changelogExists = jdbcTemplate.queryForObject(
    //        "SELECT count(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = 'databasechangelog'", 
    //        Integer.class, schemaName);
    //    assertThat(changelogExists).isEqualTo(1)
    //        .withFailMessage("Liquibase no creó la tabla DATABASECHANGELOG en el esquema.");
//
    //    verificarTablaExiste(schemaName, "users");
    //    verificarTablaExiste(schemaName, "regions");
    //    verificarTablaExiste(schemaName, "stores");
    //}
//
    //private void verificarTablaExiste (String schemaName, String tableName){
    //    // CORRECCIÓN 4: information_schema.tables en lugar de information.tables
    //    Integer tableExists = jdbcTemplate.queryForObject(
    //        "SELECT count(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?", 
    //        Integer.class, schemaName, tableName);
    //    assertThat(tableExists).isEqualTo(1)
    //        .withFailMessage("La tabla '%s' no se creó dentro del esquema '%s'.", tableName, schemaName);
    //}
}//