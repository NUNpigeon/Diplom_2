import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.UUID;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;

public class GetOrderUserTest {
    private ClUser userClient;
    private ClOrder orderClient;
    private String accessToken;
    private ValidatableResponse createdOrderResponse;

    @Before
    public void setUp() {
        userClient = new ClUser();
        orderClient = new ClOrder();

        // Генерация уникальных тестовых данных
        String uniqueEmail = "user_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        String uniqueName = "TestUser_" + UUID.randomUUID().toString().substring(0, 8);

        User testUser = new User(uniqueEmail, "secure_password123", uniqueName);


        ArrayList<String> ingredients = new ArrayList<>();
        ingredients.add("61c0c5a71f815400326104a3");
        ingredients.add("61c0c5a71f815400326104a1");
        Order order = new Order(ingredients);

        try {

            ValidatableResponse response = userClient.createUser(testUser);
            response.statusCode(SC_OK);
            accessToken = response.extract().path("accessToken");


            createdOrderResponse = orderClient.orderWithAuth(accessToken, order);
            createdOrderResponse
                    .statusCode(SC_OK)
                    .body("success", equalTo(true));

        } catch (Exception e) {
            handleError("Ошибка при подготовке тестовых данных (создание пользователя/заказа)", e);
        }
    }

    private void handleError(String message, Exception e) {
        throw new RuntimeException(message, e);
    }

    @Test
    @DisplayName("Получение заказов авторизованного пользователя")
    @Description("Get запрос на ручку api/orders")
    public void getOrderAuthUser() {
        try {
            Assert.assertNotNull("AccessToken должен быть получен", accessToken);
            Assert.assertNotNull("Заказ должен быть создан", createdOrderResponse);


            ValidatableResponse getOrdersResponse = orderClient.getOrderUserAuth(accessToken);
            getOrdersResponse
                    .statusCode(SC_OK)
                    .body("success", equalTo(true))
                    .body("orders", not(empty()))
                    .body("orders[0].ingredients", not(empty()));
        } catch (Exception e) {
            handleError("Ошибка при получении заказов авторизованного пользователя", e);
        }
    }

    @Test
    @DisplayName("Получение заказов не авторизованного пользователя")
    @Description("Get запрос на ручку api/orders")
    public void getOrderNotAuthUser() {
        try {
            Assert.assertNotNull("AccessToken должен быть получен", accessToken);
            Assert.assertNotNull("Заказ должен быть создан", createdOrderResponse);

            // Пытаемся получить заказы без авторизации
            orderClient.getOrderUserNotAuth()
                    .statusCode(SC_UNAUTHORIZED)
                    .body("success", equalTo(false))
                    .body("message", equalTo("You should be authorised"));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при проверке неавторизованного доступа", e);
        }
    }

    @After
    public void clearData() {
        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                userClient.deleteUser(accessToken);
            } catch (Exception e) {
                throw new RuntimeException("Ошибка при удалении пользователя", e);
            }
        }
    }
}
