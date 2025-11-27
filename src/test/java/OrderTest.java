import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;


import static org.hamcrest.CoreMatchers.equalTo;

public class OrderTest {
    private ClUser userClient;
    private ClOrder orderClient;
    private Order order;
    private String accessToken;
    private User user;
    private boolean isUserCreated = false;

    @Before
    public void setUp() {
        userClient = new ClUser();
        orderClient = new ClOrder();
        user = new User("testuser@example.com", "password", "TestName");

        ValidatableResponse createUserResponse = userClient.createUser(user);
        if (createUserResponse.extract().statusCode() == 200) {
            accessToken = createUserResponse.extract().path("accessToken");
            isUserCreated = true;
        } else {
            accessToken = null;
            isUserCreated = false;
        }

        ArrayList<String> ingredients = new ArrayList<>();
        ingredients.add("61c0c5a71f815400326104a3");
        ingredients.add("61c0c5a71f815400326104a1");
        order = new Order(ingredients);
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    @Description("Создание заказа с валидным accessToken и ингредиентами")
    public void createOrderWithAuthAndIngredients() {
        if (!isUserCreated) {
            System.out.println("Пользователь не был успешно создан, пропуск теста");
            return;
        }

        ValidatableResponse response = orderClient.orderWithAuth(accessToken, order);
        response.assertThat().statusCode(200).body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа без авторизации и с ингредиентами")
    @Description("Создание заказа без accessToken, но с ингредиентами")
    public void createOrderWithoutAuthAndIngredients() {
        ValidatableResponse response = orderClient.orderWithoutAuth(order);
        response.assertThat().statusCode(200).body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа с авторизацией, но без ингредиентов")
    @Description("Создание заказа с accessToken, но без ингредиентов")
    public void createOrderWithAuthAndNoIngredients() {
        if (!isUserCreated) {
            System.out.println("Пользователь не был успешно создан, пропуск теста");
            return;
        }

        Order noIngredientsOrder = new Order(new ArrayList<>());
        ValidatableResponse response = orderClient.orderWithAuth(accessToken, noIngredientsOrder);
        response.assertThat().statusCode(400).body("success", equalTo(false));
    }

    @After
    public void tearDown() {
        if (isUserCreated) {
            try {
                if (accessToken != null) {
                    String replacedToken = accessToken.replace("Bearer ", "");
                    userClient.deleteUser(replacedToken);
                }
            } catch (Exception e) {
                System.out.println("Ошибка при удалении пользователя: " + e.getMessage());
            }
        }
    }
}

