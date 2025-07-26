package com.cloudcompilerr.ai.vectordb.config;

import com.cloudcompilerr.ai.core.config.AiProperties;
import io.hypersistence.utils.hibernate.type.array.FloatArrayType;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.MetadataBuilderInitializer;
import org.hibernate.service.ServiceRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 * Configuration for vector database integration.
 */
@Configuration
@RequiredArgsConstructor
public class VectorDbConfig {

    private final AiProperties aiProperties;

    /**
     * Initializes the vector database with required extensions.
     *
     * @param dataSource The data source
     * @return DataSourceInitializer for vector database setup
     */
    @Bean
    @ConditionalOnProperty(name = "ai.vector-db.type", havingValue = "pgvector")
    public DataSourceInitializer pgvectorDatabaseInitializer(DataSource dataSource) {
        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
        resourceDatabasePopulator.addScript(new ClassPathResource("db/pgvector-init.sql"));
        
        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(dataSource);
        dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);
        return dataSourceInitializer;
    }

    /**
     * Hibernate initializer for vector types.
     */
    public static class HibernateVectorTypeInitializer implements MetadataBuilderInitializer {
        @Override
        public void contribute(MetadataBuilder metadataBuilder, StandardServiceRegistry serviceRegistry) {
            metadataBuilder.applyBasicType(FloatArrayType.INSTANCE, "vector");
        }
    }
}