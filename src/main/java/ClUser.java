
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;

public class ClUser extends PageURL {

    @Step("Создание уникального пользователя")
    public ValidatableResponse createUser(User user) {
        return given()
                .spec(getPageURL())
                .body(user)
                .when()
                .post("/api/auth/register")
                .then();
    }

    @Step("Вход под существующим пользователем")
    public ValidatableResponse loginUser(User user) {
        return given()
                .spec(getPageURL())
                .body(user)
                .when()
                .post("/api/auth/login")
                .then();
    }

    @Step("Удаление пользователя")
    public void deleteUser(String accessToken) {
        given()
                .spec(getPageURL())
                .auth().oauth2(accessToken)
                .delete("/api/auth/user")
                .then();
    }

    @Step("Изменение данных авторизованного пользователя")
    public ValidatableResponse updateUser(String accessToken, User user) {
        return given()
                .spec(getPageURL())
                .body(user)
                .auth().oauth2(accessToken)
                .patch("/api/auth/user")
                .then();
    }

    @Step("Изменение данных неавторизованного пользователя")
    public ValidatableResponse updateUserNotAuth(User user) {
        return given()
                .spec(getPageURL())
                .body(user)
                .patch("/api/auth/user")
                .then();
    }
}