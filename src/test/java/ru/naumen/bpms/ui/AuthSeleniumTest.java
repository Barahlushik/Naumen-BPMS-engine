package ru.naumen.bpms.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import ru.naumen.bpms.model.User;
import ru.naumen.bpms.model.UserRole;
import ru.naumen.bpms.repository.UserRepository;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthSeleniumTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private WebDriver driver;

    @BeforeAll
    static void beforeAll() {
        WebDriverManager.chromedriver()
                .driverVersion("147.0.7727.101")
                .setup();
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        userRepository.save(new User(
                "MrkKriger",
                "Selenium User",
                "selenium@example.com",
                UserRole.ROLE_USER,
                true,
                passwordEncoder.encode("PassdFTest")
        ));

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1280,800");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Пользователь должен успешно войти и выйти из приложения")
    void userShouldLoginAndLogoutSuccessfully() {
        String baseUrl = "http://localhost:" + port;
        driver.get(baseUrl + "/login");
        driver.findElement(By.name("username")).sendKeys("MrkKriger");
        driver.findElement(By.name("password")).sendKeys("PassdFTest");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.urlToBe(baseUrl + "/"));
        assertThat(driver.getCurrentUrl()).isEqualTo(baseUrl + "/");
        assertThat(driver.getPageSource()).contains("BPMS Home");
        driver.findElement(By.id("logout-button")).click();
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.urlContains("/login"));
        assertThat(driver.getCurrentUrl()).contains("/login");
        assertThat(driver.getPageSource()).contains("Добро пожаловать");
    }
}