import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ChangeUserDataTest {
    private ClUser clUser;
    private String accessToken;

    @Before
    public void setUp() {
        clUser = new ClUser();
        User user = new User("bskurnikova@yandex.ru", "123", "batonov");
        ValidatableResponse response = clUser.createUser(user);
        accessToken = response.extract().path("accessToken");
    }

    @Test
    @DisplayName("Изменение информации о пользователе с авторизацией. Ответ 200")
    @Description("Patch запрос на ручку /api/auth/user")
    public void updateUserWithAuth() {
        User userTwo = new User("natali@yandex.ru", "1887056", "Nata");
        clUser.updateUser(accessToken, userTwo)
                .assertThat()
                .statusCode(200);
    }

    @Test
    @DisplayName("Изменение информации о пользователе без авторизации. Ответ 401")
    @Description("Patch запрос на ручку /api/auth/user")
    public void updateUserWithoutAuth() {
        User userTwo = new User("natali@yandex.ru", "1887056", "Nata");
        clUser.updateUserNotAuth(userTwo)
                .assertThat()
                .statusCode(401);
    }

    @After
    public void clearData() {
        try {
            clUser.deleteUser(accessToken);
        } catch (Exception e) {
            System.out.println("Пользователь не удалился");
        }
    }
}
