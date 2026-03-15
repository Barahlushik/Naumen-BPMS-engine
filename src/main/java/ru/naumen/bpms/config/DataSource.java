package ru.naumen.bpms.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import ru.naumen.bpms.model.ProcessDefinition;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataSource {

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public List<ProcessDefinition> processDefinitionList() {
        return new ArrayList<>();
    }

}
