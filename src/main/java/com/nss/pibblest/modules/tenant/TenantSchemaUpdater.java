package com.nss.pibblest.modules.tenant;

import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

@Component
public class TenantSchemaUpdater implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TenantSchemaUpdater.class);

    private final JdbcTemplate jdbcTemplate;

    private final DataSource dataSource;

    public TenantSchemaUpdater(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        String sql = "SELECT schema_name from identity.owners";

        List<String> schemas;
        try {
            schemas = jdbcTemplate.queryForList(sql, String.class);
        } catch (Exception e) {

            log.warn(
                    "No se pudo consultar la tabla de owners. ¿Es la primera vez que arranca la app? Omitiendo migración masiva.");
            return;

        }

        if (schemas.isEmpty()) {
            log.info("No hay inquilinos registrados. Omitiendo actualización masiva.");
            return;
        }

        for (String schema : schemas) {
            log.info("🛠️ Revisando/Actualizando esquema: {}", schema);
            try (Connection connection = dataSource.getConnection()) {
                Database database = DatabaseFactory.getInstance()
                        .findCorrectDatabaseImplementation(new JdbcConnection(connection));

                database.setDefaultSchemaName(schema);
                database.setLiquibaseSchemaName(schema);

                try (Liquibase liquibase = new Liquibase("changelog/owners/owner-changelog.yml",
                        new ClassLoaderResourceAccessor(getClass().getClassLoader()),
                        database)) {

                    liquibase.update(new Contexts(), new LabelExpression());
                }

                
            } catch (Exception e) {

                log.error("❌ Error crítico durante la actualización masiva de inquilinos.", e);
                throw new IllegalStateException("Falló la migración masiva de inquilinos", e);

            }

        }

        log.info("✅ Actualización masiva completada exitosamente para {} inquilinos.", schemas.size());

    }

}
