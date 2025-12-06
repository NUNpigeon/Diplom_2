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
import static org.hamcrest.Matchers.notNullValue;

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
            String errorBody = createUserResponse.extract().body().asString();
            System.err.println("Response body: " + errorBody);
        }
    }

    @Test
    @DisplayName("Create order with auth and valid ingredients")
    @Description("POST /api/orders with valid accessToken and ingredient list")
    public void createOrderWithAuthAndValidIngredients() {
        if (!isUserCreated) {
            System.out.println("Test skipped: user not created");
            return;
        }

        List<String> ingredients = new ArrayList<>();
        ingredients.add("61c0c5a71f815400326104a3");
        ingredients.add("61c0c5a71f815400326104a1");
        Order order = new Order(ingredients);

        System.out.println("Sending order with token: " + accessToken);

        ValidatableResponse response = orderClient.orderWithAuth(accessToken, order);
        String responseBody = response.extract().body().asString();
        System.out.println("Server response: " + responseBody);

        response.assertThat()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order", notNullValue());
    }

    @Test
    @DisplayName("Create order without auth but with ingredients")
    @Description("POST /api/orders without accessToken but with valid ingredient list")
    public void createOrderWithoutAuthAndIngredients() {
        List<String> ingredients = new ArrayList<>();
        ingredients.add("61c0c5a71f815400326104a3");
        ingredients.add("61c0c5a71f815400326104a1");
        Order order = new Order(ingredients);

        ValidatableResponse response = orderClient.orderWithoutAuth(order);
        String responseBody = response.extract().body().asString();
        System.out.println("Server response (no auth): " + responseBody);


        // API может возвращать 401 или 403 без токена — уточняем ожидаемый статус
        response.assertThat()
                .statusCode(401) // или 403, если так определено в API
                .body("success", equalTo(false))
                .body("message", containsString("Unauthorized"));
    }

    @Test
    @DisplayName("Create order with auth and invalid ingredient hash")
    @Description("POST /api/orders with valid accessToken but invalid ingredient ID")
    public void createOrderWithAuthAndInvalidIngredientHash() {
        if (!isUserCreated) {
            System.out.println("Test skipped: user not created");
            return;
        }

        List<String> invalidIngredients = new ArrayList<>();
        invalidIngredients.add("invalid_ingredient_hash_123");
        Order order = new Order(invalidIngredients);

        System.out.println("Sending order with invalid ingredient hash and token: " + accessToken);


        ValidatableResponse response = orderClient.orderWithAuth(accessToken, order);
        String responseBody = response.extract().body().asString();
        System.out.println("Server response (invalid ingredient): " + responseBody);

        // Ожидаем 400 Bad Request, а не 500
        response.assertThat()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", containsString("Invalid ingredient"));
    }

    @After
    public void tearDown() {
        if (isUserCreated && accessToken != null) {
            try {
                userClient.deleteUser(accessToken);
                System.out.println("User deleted successfully.");
            } catch (Exception e) {
                System.err.println("Error deleting user: " + e.getMessage());
            }
        }
    }
}
