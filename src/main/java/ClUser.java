
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
                .post(Endpoints.BASE_REGISTER)
                .then();
    }

    @Step("Вход под существующим пользователем")
    public ValidatableResponse loginUser(User user) {
        return given()
                .spec(getPageURL())
                .body(user)
                .when()
                .post(Endpoints.BASE_LOGIN)
                .then();
    }

    @Step("Удаление пользователя")
    public void deleteUser(String accessToken) {
        given()
                .spec(getPageURL())
                .auth().oauth2(accessToken)
                .delete(Endpoints.BASE_USER)
                .then();
    }

    @Step("Изменение данных авторизованного пользователя")
    public ValidatableResponse updateUser(String accessToken, User user) {
        return given()
                .spec(getPageURL())
                .body(user)
                .auth().oauth2(accessToken)
                .patch(Endpoints.BASE_USER)
                .then();
    }

    @Step("Изменение данных неавторизованного пользователя")
    public ValidatableResponse updateUserNotAuth(User user) {
        return given()
                .spec(getPageURL())
                .body(user)
                .patch(Endpoints.BASE_USER)
                .then();
    }
}
