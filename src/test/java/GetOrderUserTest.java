import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;

public class GetOrderUserTest {
    private ClUser userClient;
    private ClOrder orderClient;
    private Order order;
    private String accessToken;

    @Before
    public void setUp() {
        userClient = new ClUser();
        orderClient = new ClOrder();

        // Генерация уникальных тестовых данных
        String uniqueEmail = "user_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        String uniqueName = "TestUser_" + UUID.randomUUID().toString().substring(0, 8);

        User testUser = new User(uniqueEmail, "secure_password123", uniqueName);

        // Инициализация объекта Order
        ArrayList<String> ingredients = new ArrayList<>();
        ingredients.add("61c0c5a71f815400326104a3");
        ingredients.add("61c0c5a71f815400326104a1");
        order = new Order(ingredients);

        try {
            // Получаем ответ и проверяем статус
            ValidatableResponse response = userClient.createUser(testUser);
            response.statusCode(200);
            accessToken = response.extract().path("accessToken");
        } catch (Exception e) {
            handleError("Ошибка при создании пользователя", e);
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
            Assert.assertNotNull(accessToken, "AccessToken должен быть получен");

            // Создаём заказ
            ValidatableResponse createOrderResponse = orderClient.orderWithAuth(accessToken, order);
            createOrderResponse
                    .statusCode(200)
                    .body("success", equalTo(true));

            // Получаем список заказов
            ValidatableResponse getOrdersResponse = orderClient.getOrderUserAuth(accessToken);
            getOrdersResponse
                    .statusCode(200)
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
            // Проверяем наличие токена
            Assert.assertNotNull(accessToken, "AccessToken должен быть получен");

            // Создаём заказ
            orderClient.orderWithAuth(accessToken, order)
                    .statusCode(200)
                    .body("success", equalTo(true));

            // Пытаемся получить заказы без авторизации
            orderClient.getOrderUserNotAuth()
                    .statusCode(401)
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