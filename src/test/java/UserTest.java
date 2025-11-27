import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;

public class UserTest {
    private ClUser creatingUser;
    private User user;
    private String accessToken;

    @Before
    public void setUp() {
        creatingUser = new ClUser();
        user = new User("bskurnikova@yandex.ru", "123", "batonov");
    }

    @Test
    @DisplayName("Создать уникального пользователя.")
    @Description("Post запрос на ручку /api/auth/register")
    public void createUniqueUserAndBodyTest() {
        try {
            ValidatableResponse response = creatingUser.createUser(user);
            response
                    .statusCode(200)
                    .body("user.email", equalTo(user.getEmail()))
                    .body("user.name", equalTo(user.getName()))
                    .body("accessToken", startsWith("Bearer "))
                    .body("refreshToken", notNullValue())
                    .body("success", equalTo(true));
            accessToken = response.extract().path("accessToken");
        } catch (Exception e) {
            System.err.println("Ошибка при создании пользователя: " + e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Создать пользователя, который уже зарегистрирован.")
    @Description("Post запрос на ручку /api/auth/register")
    public void createRegisteredUserAndBodyTest() {
        creatingUser.createUser(user);
        ValidatableResponse response = creatingUser.createUser(user);
        response
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Создать пользователя и не заполнить пароль.")
    @Description("Post запрос на ручку /api/auth/register")
    public void createUserWithoutPasswordTest() {
        User userWithoutPassword = new User(user.getEmail(), null, user.getName());
        ValidatableResponse response = creatingUser.createUser(userWithoutPassword);
        response
                .statusCode(403) // API возвращает 403, а не 400
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создать пользователя без заполнения поля почты.")
    @Description("Post запрос на ручку /api/auth/register")
    public void createUserWithoutEmailTest() {
        User userWithoutEmail = new User(null, user.getPassword(), user.getName());
        ValidatableResponse response = creatingUser.createUser(userWithoutEmail);
        response
                .statusCode(403)  // API возвращает 403, а не 400
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создать пользователя без заполнения поля имя пользователя.")
    @Description("Post запрос на ручку /api/auth/register")
    public void createUserWithoutNameTest() {
        User userWithoutName = new User(user.getEmail(), user.getPassword(), null);
        ValidatableResponse response = creatingUser.createUser(userWithoutName);
        response
                .statusCode(403) // API возвращает 403, а не 400
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @After
    public void clearData() {
        if (accessToken != null) {
            try {
                String accessTokenValue = accessToken.replace("Bearer ", "");
                creatingUser.deleteUser(accessTokenValue);
                System.out.println("Пользователь успешно удален.");
            } catch (Exception e) {
                System.err.println("Ошибка при удалении пользователя: " + e.getMessage());
            }
        } else {
            System.out.println("Токен отсутствует. Пользователь не был удален.");
        }
    }
}
