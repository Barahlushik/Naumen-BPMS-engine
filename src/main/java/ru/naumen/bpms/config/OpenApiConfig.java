package ru.naumen.bpms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SESSION_AUTH = "sessionAuth";

    @Bean
    public OpenAPI bpmsOpenApi(@Value("${app.version:0.0.1}") String appVersion) {
        return new OpenAPI()
                .info(new Info()
                        .title("BPMS Engine API")
                        .version(appVersion)
                        .description("REST API backend-движка для моделирования и исполнения бизнес-процессов."))
                .addSecurityItem(new SecurityRequirement().addList(SESSION_AUTH))
                .components(new Components()
                        .addSecuritySchemes(SESSION_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("Session-based authentication через Spring Security form login.")))
                .addTagsItem(new Tag().name("Process Instances").description("Пользовательские операции с экземплярами процессов."))
                .addTagsItem(new Tag().name("Admin Process Definitions").description("Администрирование определений процессов, шагов и переходов."))
                .addTagsItem(new Tag().name("Admin Users").description("Администрирование пользователей."))
                .addTagsItem(new Tag().name("Reports").description("Формирование и получение отчетов."))
                .addTagsItem(new Tag().name("Criteria Processes").description("Диагностические endpoints для выборок процессов."));
    }
}

