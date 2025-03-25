package org.itmo.testing.lab2.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.javalin.Javalin;
import io.restassured.RestAssured;
import java.time.LocalDateTime;
import java.time.YearMonth;
import org.itmo.testing.lab2.controller.UserAnalyticsController;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserAnalyticsIntegrationTest {

    private Javalin app;
    private final LocalDateTime now = LocalDateTime.now();
    private final String monthNow = YearMonth.now().toString();

    @BeforeAll
    void setUp() {
        app = UserAnalyticsController.createApp();
        int port = 1234;
        app.start(port);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @AfterAll
    void tearDown() {
        app.stop();
    }

    @Test
    @Order(1)
    @DisplayName("Тест регистрации пользователя")
    void testUserRegistration() {
        given().queryParam("userId", "user1")
                .queryParam("userName", "Alice")
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body(equalTo("User registered: true"));
    }

    @Test
    @Order(2)
    @DisplayName("Тест регистрации пользователя без id")
    void testUserRegistrationNoId() {
        given().queryParam("userName", "Alice")
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(2)
    @DisplayName("Тест регистрации пользователя без имени")
    void testUserRegistrationNoName() {
        given().queryParam("userId", "user2")
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(3)
    @DisplayName("Тест регистрации пользователя без имени")
    void testUserRegistrationExtraParam() {
        given().queryParam("userId", "user2")
                .queryParam("userName", "Bob")
                .queryParam("extra", "extra")
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body(equalTo("User registered: true"));
    }

    @Test
    @Order(4)
    @DisplayName("Тест регистрации пользователя с пустым id")
    void testUserRegistrationEmptyId() {
        given().queryParam("userId", "")
                .queryParam("userName", "Cecil")
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body(equalTo("User registered: true"));
    }

    @Test
    @Order(5)
    @DisplayName("Тест записи сессии")
    void testRecordSession() {
        given().queryParam("userId", "user1")
                .queryParam("loginTime", now.minusHours(1).toString())
                .queryParam("logoutTime", now.toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(200)
                .body(equalTo("Session recorded"));
    }

    @Test
    @Order(6)
    @DisplayName("Тест записи сессии без id")
    void testRecordSessionNoId() {
        given().queryParam("loginTime", now.minusHours(1).toString())
                .queryParam("logoutTime", now.toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(6)
    @DisplayName("Тест записи сессии без loginTime")
    void testRecordSessionNoLoginTime() {
        given().queryParam("userId", "user1")
                .queryParam("logoutTime", now.toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(6)
    @DisplayName("Тест записи сессии без logoutTime")
    void testRecordSessionNoLogoutTim() {
        given().queryParam("userId", "user1")
                .queryParam("loginTime", now.minusHours(1).toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(6)
    @DisplayName("Тест записи сессии с некорректным временем")
    void testRecordSessionInvalidTime() {
        given().queryParam("userId", "user1")
                .queryParam("loginTime", "2025-03-04")
                .queryParam("logoutTime", now.minusHours(1).toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(equalTo("Invalid data: Text '2025-03-04' could not be parsed at index 10"));
    }

    @Test
    @Order(7)
    @DisplayName("Тест записи сессии login>logout")
    void testRecordSessionMinusTime() {
        given().queryParam("userId", "user1")
                .queryParam("loginTime", now.toString())
                .queryParam("logoutTime", now.minusHours(2).toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(200)
                .body(equalTo("Session recorded"));
    }

    @Test
    @Order(7)
    @DisplayName("Тест получения общего времени активности")
    void testGetTotalActivity() {
        given().queryParam("userId", "user1")
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(200)
                .body(containsString("Total activity:"))
                .body(containsString("minutes"));
    }

    @Test
    @Order(8)
    @DisplayName("Тест получения общего времени активности с несуществующим пользователем")
    void testGetTotalActivityUserNotExist() {
        given().queryParam("userId", "notExist")
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(400)
                .body(equalTo("Invalid data: No sessions found for user"));
    }

    @Test
    @Order(8)
    @DisplayName("Тест получения общего времени активности без записей")
    void testGetTotalActivityNoSessions() {
        given().queryParam("userId", "user2")
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(400)
                .body(equalTo("Invalid data: No sessions found for user"));
    }

    @Test
    @Order(8)
    @DisplayName("Тест получения общего времени без id")
    void testGetTotalActivityNoId() {
        given().queryParam("userIds", "users")
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(400)
                .body(equalTo("Missing userId"));
    }

    @Test
    @Order(9)
    @DisplayName("Тест получения неактивных пользователей")
    void testGetInactiveUsers() {
        given().queryParam("days", 10)
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(200)
                .body(equalTo("[]"));
    }

    @Test
    @Order(10)
    @DisplayName("Тест получения неактивных пользователей без days")
    void testGetInactiveUsersNoDays() {
        given().queryParam("DAYS", 10)
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(400)
                .body(equalTo("Missing days parameter"));
    }

    @Test
    @Order(10)
    @DisplayName("Тест получения неактивных пользователей неверная дата")
    void testGetInactiveUsersInvalidDays() {
        given().queryParam("days", 5.)
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(400)
                .body(equalTo("Invalid number format for days"));
    }

    @Test
    @Order(11)
    @DisplayName("Тест получения неактивных пользователей отрицательная дата")
    void testGetInactiveUsersMinusDays() {
        given().queryParam("days", -10)
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(200)
                .body(equalTo("[\"user1\"]"));
    }

    @Test
    @Order(12)
    @DisplayName("Тест получения статистики за месяц")
    void testGetMonthlyActivity() {
        given().queryParam("userId", "user1")
                .queryParam("month", monthNow)
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(200)
                .body(not(equalTo("{}")));
    }

    @Test
    @Order(13)
    @DisplayName("Тест получения статистики за месяц без id")
    void testGetMonthlyActivityNoId() {
        given().queryParam("extraParam", "extra")
                .queryParam("month", monthNow)
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(13)
    @DisplayName("Тест получения статистики за месяц без month")
    void testGetMonthlyActivityNoMonth() {
        given().queryParam("userId", "user1")
                .queryParam("months", monthNow)
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(13)
    @DisplayName("Тест получения статистики за месяц некорректный month")
    void testGetMonthlyActivityInvalidMonth() {
        given().queryParam("userId", "user1")
                .queryParam("month", "12-2025")
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(400)
                .body(equalTo("Invalid data: Text '12-2025' could not be parsed at index 0"));
    }
}
