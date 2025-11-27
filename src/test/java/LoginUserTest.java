import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;

public class LoginUserTest {
    private ClUser userClient;
    private User user;
    private String accessToken; // Для хранения токена

    @Before
    public void setUp() {
        userClient = new ClUser(); // Инициализируем userClient
        user = new User("bskurnikova@yandex.ru", "123", "batonov");
        ValidatableResponse response = userClient.createUser(user); // Создаём пользователя и получаем ответ
        accessToken = response.extract().path("accessToken");
    }

    @Test
    @DisplayName("Логин существующего пользователя.")
    @Description("Post запрос на ручку /api/auth/login")
    public void loginWithUserTrueAndBody() {
        ValidatableResponse responseLogin = userClient.loginUser(user);
        responseLogin.assertThat().statusCode(HTTP_OK);
        responseLogin.assertThat().body("success", equalTo(true));
        responseLogin.assertThat().body("accessToken", startsWith("Bearer "))
                .and()
                .body("refreshToken", notNullValue());
        responseLogin.assertThat().body("user.email", equalTo(user.getEmail()))
                .and()
                .body("user.name", equalTo(user.getName()));
    }

    @Test
    @DisplayName("Логин с неверным адресом почты.")
    @Description("Post запрос на ручку /api/auth/login")
    public void loginWithUserFalseAndBody() {
        User invalidUser = new User("invalid@google.com", user.getPassword(), user.getName());
        ValidatableResponse loginResponse = userClient.loginUser(invalidUser);
        loginResponse.assertThat().statusCode(HTTP_UNAUTHORIZED);
        loginResponse.assertThat().body("success", equalTo(false))
                .and()
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Логин под неверным паролем.")
    @Description("Post запрос на ручку /api/auth/login")
    public void loginWithUserFalsePasswordAndBody() {
        User invalidUser = new User(user.getEmail(), "188N70a", user.getName());
        ValidatableResponse loginResponse = userClient.loginUser(invalidUser);

        loginResponse.assertThat().statusCode(HTTP_UNAUTHORIZED);
        loginResponse.assertThat().body("success", equalTo(false))
                .and()
                .body("message", equalTo("email or password are incorrect"));
    }

    @After
    public void clearData() {
        try {
            userClient.deleteUser(accessToken);
        } catch (Exception e) {
            System.out.println("Пользователь не удалился. Возможно ошибка при создании");
        }
    }
}
