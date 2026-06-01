package ru.naumen.bpms.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class EngineMetadataConfig {

    @Value("${app.name:BPMS}")
    private String name;

    @Value("${app.version:undefined}")
    private String version;

    @PostConstruct
    public void printMetadata() {
        log.info("BPMS Engine metadata: projectName={}, version={}", name, version);
    }
}
