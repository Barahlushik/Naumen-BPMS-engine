package ru.naumen.bpms.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EngineMetadataConfig {

    @Value("${app.name}")
    private String name;

    @Value("${app.version}")
    private String version;

    @PostConstruct
    public void printMetadata() {

        System.out.println();
        System.out.println("=================================");
        System.out.println("project name: " + name);
        System.out.println("version: " + version);
        System.out.println("=================================");
        System.out.println();

    }
}
