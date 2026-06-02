package com.nss.pibblest.modules.notifications.internal.core.owner;

import java.sql.Connection;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.nss.pibblest.modules.owners.api.events.OwnerRegisteredEvent;
import com.nss.pibblest.modules.owners.api.events.TenantSchemaReadyEvent;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

@Component
public class OwnerSchemaProvisionerListener {

    private final Logger log = LoggerFactory.getLogger(OwnerSchemaProvisionerListener.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final ApplicationEventPublisher events;

    public OwnerSchemaProvisionerListener(JdbcTemplate jdbcTemplate, DataSource dataSource, ApplicationEventPublisher events) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
        this.events = events;
    }

    @KafkaListener(topics = "owners-registered-topic", groupId = "provisioning-group")
    public void provisionNewOwnerDb(OwnerRegisteredEvent event) {
        String schemaName = event.schemaName();

        log.info("⚙️ Iniciando aprovisionamiento para el Owner: {} en el esquema: {}", event.company(), schemaName);

        try {

            String createSchemaSql = String.format("CREATE SCHEMA IF NOT EXISTS \"%s\"", schemaName);

            jdbcTemplate.execute(createSchemaSql);
            log.info("✅ Esquema '{}' creado exitosamente.", schemaName);

            runLiquibaseMigrations(schemaName);

            // ¡Saga completada! Disparamos el evento para que el módulo de empleados sincronice al Owner
            events.publishEvent(new TenantSchemaReadyEvent(
                event.schemaName(),
                event.email(),
                event.encodedPassword(),
                event.name(),
                event.lastName()
            ));

            log.info("🚀 Aprovisionamiento completado con éxito para '{}'.", schemaName);

        } catch (Exception e) {
            log.error("❌ Error crítico aprovisionando el esquema para el Owner {}: {}", event.company(), e.getMessage(),
                    e);

            throw new RuntimeException("Fallo en la provisión del inquilino", e);
        }
    }

    private void runLiquibaseMigrations(String schemaName) throws Exception {

        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setDefaultSchemaName(schemaName);
            database.setLiquibaseSchemaName(schemaName);
            String changelogFile = "changelog/owners/owner-changelog.yml";
            Liquibase liquibase = new Liquibase(
                    changelogFile,
                    new ClassLoaderResourceAccessor(getClass().getClassLoader()),
                    database);
            liquibase.update("");
            log.info("✅ Tablas creadas mediante Liquibase en el esquema '{}'.", schemaName);
        }

    }

}
