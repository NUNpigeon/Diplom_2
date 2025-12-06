import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import net.datafaker.Faker;


import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.*;

public class LoginUserTest {
    private ClUser userClient;
    private Faker faker;
    private User testUser;
    private String accessToken;

    @Before
    public void setUp() {
        userClient = new ClUser();
        faker = new Faker();
    }

    @Test
    @DisplayName("Логин существующего пользователя")
    @Description("Post запрос на ручку /api/auth/login")
    public void loginWithUserTrueAndBody() {
        testUser = new User(
                faker.internet().emailAddress(),
                faker.regexify("[a-zA-Z0-9]{8,12}"),
                faker.name().firstName()
        );

        ValidatableResponse createResponse = userClient.createUser(testUser);
        createResponse.assertThat().statusCode(HTTP_OK);
        accessToken = createResponse.extract().path("accessToken");

        ValidatableResponse loginResponse = userClient.loginUser(testUser);
        loginResponse.assertThat()
                .statusCode(HTTP_OK)
                .body("success", equalTo(true))
                .body("accessToken", startsWith("Bearer "))
                .body("refreshToken", notNullValue())
                .body("user.email", equalTo(testUser.getEmail()))
                .body("user.name", equalTo(testUser.getName()));
    }

    @Test
    @DisplayName("Логин с неверным адресом почты")
    @Description("Post запрос на ручку /api/auth/login")
    public void loginWithUserFalseAndBody() {
        testUser = new User(
                faker.internet().emailAddress(),
                "secure_password_123",
                faker.name().firstName()
        );

        ValidatableResponse createResponse = userClient.createUser(testUser);
        createResponse.assertThat().statusCode(HTTP_OK);

        User invalidUser = new User(
                "invalid_" + faker.internet().emailAddress(),
                testUser.getPassword(),
                testUser.getName()
        );

        ValidatableResponse loginResponse = userClient.loginUser(invalidUser);
        loginResponse.assertThat()
                .statusCode(HTTP_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Логин под неверным паролем")
    @Description("Post запрос на ручку /api/auth/login")
    public void loginWithUserFalsePasswordAndBody() {
        testUser = new User(
                faker.internet().emailAddress(),
                "secure_password_123",
                faker.name().firstName()
        );

        ValidatableResponse createResponse = userClient.createUser(testUser);
        createResponse.assertThat().statusCode(HTTP_OK);

        User invalidUser = new User(
                testUser.getEmail(),
                "wrong_password_" + faker.random().nextInt(100, 999), // Исправлено: random().nextInt()
                testUser.getName()
        );

        ValidatableResponse loginResponse = userClient.loginUser(invalidUser);
        loginResponse.assertThat()
                .statusCode(HTTP_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @After
    public void clearData() {
        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                userClient.deleteUser(accessToken);
            } catch (Exception e) {
                System.err.println("Ошибка при удалении пользователя: " + e.getMessage());
            }
        }
    }
}

