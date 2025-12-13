import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;


public class OrderTest {
    private ClUser userClient;
    private ClOrder orderClient;
    private String accessToken;
    private boolean isUserCreated;

    @Before
    public void setUp() {
        userClient = new ClUser();
        orderClient = new ClOrder();
        isUserCreated = false;

        User user = new User(
                "testuser" + System.currentTimeMillis() + "@example.com",
                "password123",
                "TestName" + System.currentTimeMillis()
        );

        ValidatableResponse createUserResponse = userClient.createUser(user);
        int statusCode = createUserResponse.extract().statusCode();

        if (statusCode == 200) {
            String rawToken = createUserResponse.extract().path("accessToken");
            accessToken = rawToken.replace("Bearer ", "");
            isUserCreated = true;
            System.out.println("User created. Token: " + accessToken);
        } else {
            System.err.println("User creation failed. Status: " + statusCode);
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией")
    @Description("Создание заказа с токеном авторизации и валидными ингредиентами.")
    public void createOrderWithAuth() {

        if (!isUserCreated) return;

        List<String> ingredients = List.of("61c0c5a71f815400326104a3", "61c0c5a71f815400326104a1");
        ValidatableResponse response = orderClient.orderWithAuth(accessToken, new Order(ingredients));

        response.assertThat().statusCode(200).body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("Создание заказа без токена авторизации, проверяем ошибку.")
    public void createOrderWithoutAuth() {
        List<String> ingredients = List.of("61c0c5a71f815400326104a3", "61c0c5a71f815400326104a1");
        ValidatableResponse response = orderClient.orderWithoutAuth(new Order(ingredients));

        response.assertThat().statusCode(401).body("message", containsString("Unauthorized"));
    }

    @Test
    @DisplayName("Создание заказа с ингредиентами")
    @Description("Создание заказа с валидными ингредиентами.")
    public void createOrderWithIngredients() {
        if (!isUserCreated) return;

        List<String> ingredients = List.of("61c0c5a71f815400326104a3", "61c0c5a71f815400326104a1");
        ValidatableResponse response = orderClient.orderWithAuth(accessToken, new Order(ingredients));

        response.assertThat().statusCode(200).body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Создание заказа без ингредиентов и проверка, что возвращается ошибка.")
    public void createOrderWithoutIngredients() {
        if (!isUserCreated) return;

        ValidatableResponse response = orderClient.orderWithAuth(accessToken, new Order(new ArrayList<>()));

        response.assertThat().statusCode(400).body("message", containsString("Ingredient ids must be provided"));

    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    @Description("Создание заказа с неверными хешами ингредиентов, проверяем ошибку.")
    public void createOrderWithInvalidIngredientHash() {
        if (!isUserCreated) return;

        List<String> invalidIngredients = List.of("invalid_hash");
        ValidatableResponse response = orderClient.orderWithAuth(accessToken, new Order(invalidIngredients));

        response.assertThat().statusCode(400).body("message", containsString("Invalid ingredient"));
    }


    @After
    public void tearDown() {
        if (isUserCreated && accessToken != null) {
            userClient.deleteUser(accessToken);
        }
    }
}
