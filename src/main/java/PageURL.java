import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class PageURL {
    private static final String PAGE_URL = "https://stellarburgers.education-services.ru/";

    public RequestSpecification getPageURL() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri(PAGE_URL)
                .build();
    }
}