import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Order {
    private List<String> ingredients;

    public Order(ArrayList<String> ingredients) {
        this.ingredients = ingredients;
    }

    // Исправленный метод setIngredients
    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }
}