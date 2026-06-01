package com.nss.pibblest.shared.infrastructure;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

@Configuration
@EnableJpaRepositories(bootstrapMode = BootstrapMode.DEFERRED, basePackages="com.nss.pibblest")
public class JpaConfig {

}