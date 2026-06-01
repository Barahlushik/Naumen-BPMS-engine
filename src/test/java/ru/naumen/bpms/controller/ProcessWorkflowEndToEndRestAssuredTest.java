package ru.naumen.bpms.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import ru.naumen.bpms.model.User;
import ru.naumen.bpms.model.UserRole;
import ru.naumen.bpms.repository.*;
import ru.naumen.bpms.testsupport.PostgreSqlTestContainerSupport;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProcessWorkflowEndToEndRestAssuredTest extends PostgreSqlTestContainerSupport {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProcessInstanceRepository processInstanceRepository;

    @Autowired
    private TransitionRepository transitionRepository;

    @Autowired
    private StepDefinitionRepository stepDefinitionRepository;

    @Autowired
    private ProcessDefinitionRepository processDefinitionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        processInstanceRepository.deleteAll();
        transitionRepository.deleteAll();
        stepDefinitionRepository.deleteAll();
        processDefinitionRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.save(new User(
                "admin",
                "Admin User",
                "admin@example.com",
                UserRole.ROLE_ADMIN,
                true,
                passwordEncoder.encode("AdminPassword")
        ));

        userRepository.save(new User(
                "worker",
                "Worker User",
                "worker@example.com",
                UserRole.ROLE_USER,
                true,
                passwordEncoder.encode("WorkerPassword")
        ));
    }

    @Test
    @DisplayName("BPMS workflow должен проходить полный REST-сценарий от моделирования до завершения")
    void processWorkflow_shouldPassFullRestScenario() {
        Cookie adminSession = login("admin", "AdminPassword");
        Cookie userSession = login("worker", "WorkerPassword");

        Integer processDefinitionId = given()
                .cookie(adminSession)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "title", "Document approval",
                        "description", "Document approval workflow",
                        "category", "Docs"
                ))
                .when()
                .post("/api/admin/process-definitions")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("title", equalTo("Document approval"))
                .extract()
                .path("id");

        Integer startStepId = createStep(adminSession, processDefinitionId, "Start", "START_EVENT");
        Integer reviewStepId = createStep(adminSession, processDefinitionId, "Review", "USER_TASK");
        Integer endStepId = createStep(adminSession, processDefinitionId, "End", "END_EVENT");

        Integer startToReviewTransitionId = createTransition(
                adminSession,
                processDefinitionId,
                startStepId,
                reviewStepId,
                "start_to_review"
        );
        Integer reviewToEndTransitionId = createTransition(
                adminSession,
                processDefinitionId,
                reviewStepId,
                endStepId,
                "review_to_end"
        );

        given()
                .cookie(adminSession)
                .when()
                .post("/api/admin/process-definitions/{id}/validate", processDefinitionId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("processDefinitionId", equalTo(processDefinitionId))
                .body("valid", equalTo(true))
                .body("errors", empty());

        Integer processInstanceId = given()
                .cookie(userSession)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "processDefinitionId", processDefinitionId,
                        "startStepId", startStepId
                ))
                .when()
                .post("/api/process-instances/start")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("processDefinitionId", equalTo(processDefinitionId))
                .body("currentStepId", equalTo(startStepId))
                .body("status", equalTo("RUNNING"))
                .extract()
                .path("id");

        given()
                .cookie(userSession)
                .when()
                .get("/api/process-instances/my")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", hasItem(processInstanceId));

        given()
                .cookie(userSession)
                .when()
                .get("/api/process-instances/{id}/available-transitions", processInstanceId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", hasItem(startToReviewTransitionId));

        given()
                .cookie(userSession)
                .when()
                .post("/api/process-instances/{id}/transitions/{transitionId}/execute",
                        processInstanceId,
                        startToReviewTransitionId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(processInstanceId))
                .body("currentStepId", equalTo(reviewStepId))
                .body("status", equalTo("RUNNING"));

        given()
                .cookie(userSession)
                .when()
                .post("/api/process-instances/{id}/transitions/{transitionId}/execute",
                        processInstanceId,
                        reviewToEndTransitionId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(processInstanceId))
                .body("currentStepId", equalTo(endStepId))
                .body("status", equalTo("RUNNING"));

        given()
                .cookie(userSession)
                .when()
                .post("/api/process-instances/{id}/complete", processInstanceId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(processInstanceId))
                .body("status", equalTo("COMPLETED"))
                .body("currentStepId", nullValue());
    }

    private Integer createStep(Cookie adminSession,
                               Integer processDefinitionId,
                               String name,
                               String type) {
        return given()
                .cookie(adminSession)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", name,
                        "type", type
                ))
                .when()
                .post("/api/admin/process-definitions/{id}/steps", processDefinitionId)
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("name", equalTo(name))
                .body("type", equalTo(type))
                .extract()
                .path("id");
    }

    private Integer createTransition(Cookie adminSession,
                                     Integer processDefinitionId,
                                     Integer fromStepId,
                                     Integer toStepId,
                                     String name) {
        return given()
                .cookie(adminSession)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "fromStepId", fromStepId,
                        "toStepId", toStepId,
                        "name", name
                ))
                .when()
                .post("/api/admin/process-definitions/{id}/transitions", processDefinitionId)
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("name", equalTo(name))
                .body("fromStepId", equalTo(fromStepId))
                .body("toStepId", equalTo(toStepId))
                .extract()
                .path("id");
    }

    private Cookie login(String username, String password) {
        return given()
                .contentType(ContentType.URLENC)
                .formParam("username", username)
                .formParam("password", password)
                .redirects()
                .follow(false)
                .when()
                .post("/login")
                .then()
                .statusCode(anyOf(equalTo(302), equalTo(200)))
                .extract()
                .detailedCookie("JSESSIONID");
    }
}

