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
import ru.naumen.bpms.repository.ReportRepository;
import ru.naumen.bpms.repository.UserRepository;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReportControllerRestAssuredTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        reportRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.save(new User(
                "restuser",
                "Rest User",
                "restuser@example.com",
                UserRole.ROLE_USER,
                true,
                passwordEncoder.encode("PasswordForTest")
        ));
    }

    @Test
    @DisplayName("POST /api/reports должен создать отчет и вернуть 202")
    void createReport_shouldReturnAcceptedAndReportId() {
        Cookie session = login();

        given()
                .cookie(session)
                .when()
                .post("/api/reports")
                .then()
                .statusCode(202)
                .contentType(ContentType.JSON)
                .body("reportId", notNullValue())
                .body("status", equalTo("CREATED"))
                .body("message", containsString("асинхронное"));
    }

    @Test
    @DisplayName("GET /api/reports/{id} должен вернуть информацию об отчете")
    void getReportInfo_shouldReturnReportInfo() {
        Cookie session = login();

        Integer reportId = given()
                .cookie(session)
                .when()
                .post("/api/reports")
                .then()
                .statusCode(202)
                .extract()
                .path("reportId");

        given()
                .cookie(session)
                .when()
                .get("/api/reports/{id}", reportId)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(202)))
                .contentType(ContentType.JSON)
                .body("id", equalTo(reportId))
                .body("status", anyOf(equalTo("CREATED"), equalTo("COMPLETED"), equalTo("ERROR")))
                .body("contentReady", anyOf(equalTo(true), equalTo(false)));
    }

    @Test
    @DisplayName("GET /api/reports/{id}/content должен вернуть HTML")
    void getReportContent_shouldReturnHtml() throws InterruptedException {
        Cookie session = login();

        Integer reportId = given()
                .cookie(session)
                .when()
                .post("/api/reports")
                .then()
                .statusCode(202)
                .extract()
                .path("reportId");

        waitReportGeneration(session, reportId);

        given()
                .cookie(session)
                .when()
                .get("/api/reports/{id}/content", reportId)
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(202), equalTo(500)))
                .contentType(containsString("text/html"))
                .body(containsString("<html"));
    }

    @Test
    @DisplayName("GET /api/reports/{id} должен вернуть 404, если отчет не найден")
    void getReportInfo_shouldReturn404WhenReportNotFound() {
        Cookie session = login();

        given()
                .cookie(session)
                .when()
                .get("/api/reports/{id}", 999999L)
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("errorCode", equalTo("REPORT_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/reports без авторизации должен редиректить на login")
    void createReport_shouldRedirectToLoginWhenUnauthorized() {
        given()
                .redirects()
                .follow(false)
                .when()
                .post("/api/reports")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    private Cookie login() {
        return given()
                .contentType(ContentType.URLENC)
                .formParam("username", "restuser")
                .formParam("password", "PasswordForTest")
                .redirects()
                .follow(false)
                .when()
                .post("/login")
                .then()
                .statusCode(anyOf(equalTo(302), equalTo(200)))
                .extract()
                .detailedCookie("JSESSIONID");
    }

    private void waitReportGeneration(Cookie session, Integer reportId) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            String status = given()
                    .cookie(session)
                    .when()
                    .get("/api/reports/{id}", reportId)
                    .then()
                    .extract()
                    .path("status");

            if ("COMPLETED".equals(status) || "ERROR".equals(status)) {
                return;
            }

            Thread.sleep(200);
        }
    }
}