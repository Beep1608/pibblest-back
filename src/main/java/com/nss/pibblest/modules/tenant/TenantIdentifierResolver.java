package com.nss.pibblest.modules.tenant;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;


@Component
public class TenantIdentifierResolver implements MultiTenantConnectionProvider<String>{

    private DataSource dataSource;

    public TenantIdentifierResolver(DataSource dataSource){
        this.dataSource = dataSource;
    }


    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return getConnection("identity");
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
       connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setSchema(tenantIdentifier);
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
       connection.setSchema("identity");
       connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
       return false;
    }

    
}
