package ru.naumen.bpms;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class BpmsEngineEntryPoint {

    public static void main(String[] args) {
        log.info("BPMS Engine application startup requested.");
        SpringApplication.run(BpmsEngineEntryPoint.class, args);
        log.info("BPMS Engine application started.");
    }

}
