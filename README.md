# BPMS Engine

BPMS Engine - backend-движок для моделирования, запуска и исполнения бизнес-процессов.

Процесс описывается как ориентированный граф:

- `StepDefinition` - вершина графа, шаг процесса.
- `Transition` - ребро графа, допустимый переход между шагами.
- `ProcessDefinition` - описание процесса.
- `ProcessInstance` - запущенный экземпляр процесса.
- `User` - пользователь системы с ролью `ROLE_USER` или `ROLE_ADMIN`.

## Возможности

- создание и редактирование определений процессов;
- добавление шагов и переходов;
- валидация структуры процесса перед запуском;
- запуск экземпляра процесса пользователем;
- просмотр пользователем своих экземпляров процессов;
- просмотр администратором всех экземпляров;
- выполнение допустимых переходов между шагами;
- завершение и отмена экземпляра процесса;
- управление пользователями через admin REST API;
- session-based аутентификация и role-based авторизация;
- OpenAPI/Swagger документация;
- Liquibase-миграции для PostgreSQL;
- логирование через SLF4J/Logback и Lombok `@Slf4j`;
- Actuator endpoints для мониторинга;
- unit, integration, security и end-to-end тесты.

## Стек

- Java 21
- Spring Boot 3
- Spring MVC
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- Liquibase
- Maven
- Lombok
- MapStruct
- springdoc-openapi
- JUnit 5, Mockito, Spring Security Test, RestAssured, Testcontainers
- Docker / Docker Compose

## Архитектура

Проект разделен по слоям:

- `controller` - REST и MVC endpoints;
- `service` - бизнес-логика и проверки;
- `repository` - доступ к данным;
- `model` - JPA-сущности и enum-типы;
- `controller.dto` - request/response DTO;
- `controller.mapper` - преобразование entity <-> DTO;
- `config` - security, OpenAPI, web и прочая конфигурация.

Контроллеры не обращаются к репозиториям напрямую: работа с данными идет через service layer.

## Требования

- JDK 21
- Maven 3.9+
- Docker и Docker Compose, если нужен PostgreSQL в контейнере или Testcontainers

Проверить окружение:

```bash
java -version
mvn -version
docker --version
docker compose version
```

## Конфигурация

Основные настройки находятся в `src/main/resources/application.properties`.

Переменные окружения:

| Переменная | Значение по умолчанию | Назначение |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/bpms` | JDBC URL PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `bpms_user` | пользователь БД |
| `SPRING_DATASOURCE_PASSWORD` | `bpms_password` | пароль БД |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `validate` | режим Hibernate schema management |
| `SERVER_PORT` | `8080` | порт приложения |

Liquibase включен по умолчанию и применяет миграции из:

```text
src/main/resources/db/changelog/db.changelog-master.yaml
```

## Запуск через Docker Compose

Запустить PostgreSQL, приложение и pgweb:

```bash
docker compose up --build
```

Сервисы:

- приложение: `http://localhost:8080`
- pgweb: `http://localhost:8081`
- PostgreSQL: `localhost:5432`

Остановить сервисы:

```bash
docker compose down
```

Остановить сервисы и удалить volume с данными:

```bash
docker compose down -v
```

## Локальный запуск

Поднять только PostgreSQL:

```bash
docker compose up -d postgres
```

Запустить приложение локально:

```bash
mvn spring-boot:run
```

Если используется нестандартный путь к JDK:

```bash
JAVA_HOME=/path/to/jdk-21 mvn spring-boot:run
```

## Пользователи и роли

В системе используются роли:

- `ROLE_USER` - обычный пользователь;
- `ROLE_ADMIN` - администратор.

Регистрация доступна по адресу:

```text
GET /register
POST /register
```

Новый пользователь создается с ролью `ROLE_USER`.

Для первого администратора можно:

1. Зарегистрировать пользователя через `/register`.
2. Повысить роль в PostgreSQL:

```sql
UPDATE users
SET role = 'ROLE_ADMIN'
WHERE username = 'admin';
```

После этого пользователь сможет работать с `/api/admin/**` и Swagger UI.

## Авторизация

Проект использует session-based form login.

Публичные endpoints:

- `/login`
- `/register`
- `/css/**`

Требуют роль администратора:

- `/api/admin/**`
- `/swagger-ui/**`
- `/v3/api-docs/**`

Остальные endpoints требуют аутентификации.

Пример login через `curl` с сохранением cookie:

```bash
curl -i -c cookies.txt \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin" \
  http://localhost:8080/login
```

Дальше cookie можно передавать так:

```bash
curl -b cookies.txt http://localhost:8080/api/admin/process-definitions
```

## OpenAPI / Swagger

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

Доступ к Swagger закрыт ролью `ROLE_ADMIN`.

## Основные REST endpoints

Admin API:

| Метод | Endpoint | Назначение |
| --- | --- | --- |
| `GET` | `/api/admin/process-definitions` | список процессов |
| `GET` | `/api/admin/process-definitions/{id}` | процесс по id |
| `POST` | `/api/admin/process-definitions` | создать процесс |
| `PUT` | `/api/admin/process-definitions/{id}` | обновить процесс |
| `DELETE` | `/api/admin/process-definitions/{id}` | удалить процесс |
| `POST` | `/api/admin/process-definitions/{id}/steps` | добавить шаг |
| `POST` | `/api/admin/process-definitions/{id}/transitions` | добавить переход |
| `POST` | `/api/admin/process-definitions/{id}/validate` | проверить структуру процесса |
| `GET` | `/api/admin/process-instances` | все экземпляры процессов |
| `GET` | `/api/admin/users` | список пользователей |
| `POST` | `/api/admin/users` | создать пользователя |
| `PUT` | `/api/admin/users/{id}` | обновить пользователя |
| `DELETE` | `/api/admin/users/{id}` | удалить пользователя |

User API:

| Метод | Endpoint | Назначение |
| --- | --- | --- |
| `POST` | `/api/process-instances/start` | запустить процесс |
| `GET` | `/api/process-instances/my` | свои экземпляры процессов |
| `GET` | `/api/process-instances/my?status=RUNNING` | свои экземпляры с фильтром по статусу |
| `GET` | `/api/process-instances/{id}/available-transitions` | доступные переходы |
| `POST` | `/api/process-instances/{id}/transitions/{transitionId}/execute` | выполнить переход |
| `POST` | `/api/process-instances/{id}/complete` | завершить процесс |
| `POST` | `/api/process-instances/{id}/cancel` | отменить процесс |

Reports API:

| Метод | Endpoint | Назначение |
| --- | --- | --- |
| `POST` | `/api/reports` | создать отчет |
| `GET` | `/api/reports/{id}` | получить метаданные отчета |
| `GET` | `/api/reports/{id}/content` | получить HTML-содержимое отчета |

## Типы шагов и статусы

Типы шагов:

- `START_EVENT`
- `USER_TASK`
- `SERVICE_TASK`
- `GATEWAY`
- `END_EVENT`

Статусы экземпляров процесса:

- `RUNNING`
- `COMPLETED`
- `CANCELLED`

## Пример полного REST-сценария

Создать определение процесса:

```bash
curl -s -b cookies.txt \
  -H "Content-Type: application/json" \
  -d '{"title":"Vacation approval","description":"Vacation request workflow","category":"HR"}' \
  http://localhost:8080/api/admin/process-definitions
```

Добавить стартовый шаг:

```bash
curl -s -b cookies.txt \
  -H "Content-Type: application/json" \
  -d '{"name":"Start","type":"START_EVENT"}' \
  http://localhost:8080/api/admin/process-definitions/1/steps
```

Добавить пользовательскую задачу:

```bash
curl -s -b cookies.txt \
  -H "Content-Type: application/json" \
  -d '{"name":"Manager approval","type":"USER_TASK"}' \
  http://localhost:8080/api/admin/process-definitions/1/steps
```

Добавить конечный шаг:

```bash
curl -s -b cookies.txt \
  -H "Content-Type: application/json" \
  -d '{"name":"End","type":"END_EVENT"}' \
  http://localhost:8080/api/admin/process-definitions/1/steps
```

Добавить переходы:

```bash
curl -s -b cookies.txt \
  -H "Content-Type: application/json" \
  -d '{"fromStepId":1,"toStepId":2,"name":"Submit","condition":"submitted"}' \
  http://localhost:8080/api/admin/process-definitions/1/transitions

curl -s -b cookies.txt \
  -H "Content-Type: application/json" \
  -d '{"fromStepId":2,"toStepId":3,"name":"Approve","condition":"approved"}' \
  http://localhost:8080/api/admin/process-definitions/1/transitions
```

Проверить структуру процесса:

```bash
curl -s -b cookies.txt \
  -X POST \
  http://localhost:8080/api/admin/process-definitions/1/validate
```

Запустить процесс:

```bash
curl -s -b cookies.txt \
  -H "Content-Type: application/json" \
  -d '{"processDefinitionId":1,"startStepId":1}' \
  http://localhost:8080/api/process-instances/start
```

Посмотреть доступные переходы:

```bash
curl -s -b cookies.txt \
  http://localhost:8080/api/process-instances/1/available-transitions
```

Выполнить переход:

```bash
curl -s -b cookies.txt \
  -X POST \
  http://localhost:8080/api/process-instances/1/transitions/1/execute
```

Завершить процесс:

```bash
curl -s -b cookies.txt \
  -X POST \
  http://localhost:8080/api/process-instances/1/complete
```

## Валидация процесса

Валидация структуры проверяет, что:

- у процесса есть шаги;
- есть стартовый шаг `START_EVENT`;
- есть конечный шаг `END_EVENT`;
- переходы ссылаются на шаги текущего процесса;
- из стартового шага достижим хотя бы один конечный шаг;
- недостижимые шаги отражаются в предупреждениях.

Запуск экземпляра процесса блокируется, если определение процесса невалидно.

## Тесты

Запустить все тесты:

```bash
mvn test
```

Запустить компиляцию без тестов:

```bash
mvn -DskipTests compile
```

Запустить тесты валидации процесса:

```bash
mvn -Dtest=ProcessDefinitionValidationServiceImplTest test
```

Запустить security-тесты:

```bash
mvn -Dtest=SecurityConfigTest,UserProcessInstanceControllerSecurityTest test
```

Запустить end-to-end REST workflow:

```bash
mvn -Dtest=ProcessWorkflowEndToEndRestAssuredTest test
```

Интеграционные и e2e-тесты используют Testcontainers PostgreSQL, поэтому для них нужен доступный Docker.

## Логирование

Логи пишутся в консоль через SLF4J/Logback.

Текущий шаблон:

```text
%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n
```

Уровень логирования для приложения:

```properties
logging.level.ru.naumen.bpms=DEBUG
```

Критичность логов разделяется по смыслу:

- `DEBUG` - технические детали выполнения;
- `INFO` - успешные бизнес-операции и состояние системы;
- `WARN` - бизнес-ошибки, отказ в доступе, невалидные действия;
- `ERROR` - неожиданные системные ошибки.

Бизнес-ошибки обрабатываются через exception handling и не должны приводить к падению приложения.

## Мониторинг

Spring Actuator включен.

Health endpoint:

```text
http://localhost:8080/actuator/health
```

В конфигурации открыты все Actuator endpoints:

```properties
management.endpoints.web.exposure.include=*
```

Для production-среды это значение стоит ограничить.

## Документация и диаграммы

В проекте есть ER-диаграмма:

```text
bpms-engine-ER-diagramm.puml
bpms-engine-ER-diagramm.png
```

OpenAPI-описание доступно через `/v3/api-docs`.

Дополнительная документация по отчетам находится в:

```text
README-reports.md
```

## Соответствие ТЗ и текущие ограничения

Реализовано:

- REST API для пользовательских и административных сценариев;
- слоистая архитектура Controller / Service / Repository / Domain;
- PostgreSQL, Hibernate/JPA и Liquibase;
- session-based аутентификация;
- role-based авторизация;
- request DTO и bean validation;
- хранение паролей через hash;
- OpenAPI/Swagger;
- логирование;
- Actuator;
- unit, integration, security и e2e-тесты.

Ограничения относительно ТЗ:

- JWT не реализован, используется session-based security;
- MongoDB не подключена к runtime-функциональности;
- стратегия расширяемых шагов пока представлена enum-типами, без полноценного strategy engine;
- события и триггеры описаны как потенциальное расширение, но не реализованы;
- UML class diagram пока не оформлена отдельным артефактом.
