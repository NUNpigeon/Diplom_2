import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;

public class ClOrder extends PageURL {

    @Step("Создание заказа с авторизацией клиента")
    public ValidatableResponse orderWithAuth(String accessToken, Order order) {
        return given().spec(getPageURL()).body(order).auth().oauth2(accessToken).post("/api/orders").then();
    }

    @Step("Создание заказа без авторизации клиента")
    public ValidatableResponse orderWithoutAuth(Order order) {
        return given().spec(getPageURL()).body(order).post("/api/orders").then();
    }

    @Step("Получение информации о заказе не авторизированного пользователя")
    public ValidatableResponse getOrderUserNotAuth() {
        return given().spec(getPageURL()).get("/api/orders").then();
    }

    @Step("Получение информации о заказе авторизированного пользователя")
    public ValidatableResponse getOrderUserAuth(String accessToken) {
        return given().spec(getPageURL()).auth().oauth2(accessToken).get("/api/orders").then();
    }
}
