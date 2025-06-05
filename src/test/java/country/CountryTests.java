package country;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import model.Country;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CountryTests {
    private static final String GET_COUNTRIES_API = "/api/v1/countries";
    private static final String GET_COUNTRY_API = "/api/v1/countries/{code}";
    private static final String GET_COUNTRY_WITH_FILTER_API = "/api/v3/countries";
    private static final String X_POWERED_BY_HEADER = "X-Powered-By";
    private static final String X_POWERED_BY_HEADER_VALUE = "Express";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/json; charset=utf-8";
    private static final String GDP_FILTER = "gdp";
    private static final String OPERATOR_FILTER = "operator";

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    @Test
    void verifySchemaOfGetCountriesApi() {
        RestAssured.given().log().all()
                .get(GET_COUNTRIES_API)
                .then()
                .log().all()
                .statusCode(200)
                .assertThat().body(matchesJsonSchemaInClasspath("json-schema/countries-schema.json"));
    }

    @Test
    void verifyGetCountriesApiData() throws JsonProcessingException {
        Response response = RestAssured.given().log().all()
                .get(GET_COUNTRIES_API);
        //1. Verify status
        response.then().log().all().statusCode(200);
        //2. Verify headers
        response.then().header(X_POWERED_BY_HEADER, equalTo(X_POWERED_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(CONTENT_TYPE_HEADER_VALUE));
        //3. Verify body
        ObjectMapper mapper = new ObjectMapper();
        List<Country> expected = mapper.readValue(CountriesData.ALL_COUNTRIES_DATA, new TypeReference<>() {
        });
        List<Country> actual = response.body().as(new TypeRef<>() {
        });
        assertThat(actual.size(), equalTo(expected.size()));
        assertThat(actual.containsAll(expected), equalTo(true));
        assertThat(expected.containsAll(actual), equalTo(true));
    }

    @Test
    void verifySchemaOfGetCountryApi() {
        RestAssured.given().log().all()
                .get(GET_COUNTRY_API, "VN")
                .then()
                .log().all()
                .statusCode(200)
                .assertThat().body(matchesJsonSchemaInClasspath("json-schema/country-schema.json"));
    }

    static Stream<Country> countryProvider() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Country> inputData = mapper.readValue(CountriesData.ALL_COUNTRIES_DATA, new TypeReference<>() {
        });
        return inputData.stream();
    }

    @ParameterizedTest
    @MethodSource("countryProvider")
    void verifyGetCountry(Country input) {
        Response response = RestAssured.given().log().all()
                .get(GET_COUNTRY_API, input.getCode());
        //1. Verify status
        response.then().log().all().statusCode(200);
        //2. Verify headers
        response.then().header(X_POWERED_BY_HEADER, equalTo(X_POWERED_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(CONTENT_TYPE_HEADER_VALUE));
        //3. Verify body
        Country actual = response.body().as(Country.class);
        assertThat(actual, equalToObject(input));
    }

    @Test
    void verifySchemaOfGetCountryApiWithFilter() {
        RestAssured.given().log().all()
                .queryParam(GDP_FILTER, 5000)
                .queryParam(OPERATOR_FILTER, ">")
                .get(GET_COUNTRY_WITH_FILTER_API)
                .then()
                .log().all()
                .statusCode(200)
                .assertThat().body(matchesJsonSchemaInClasspath("json-schema/country-with-filters-schema.json"));
    }

    @Test
    void verifyGetCountryApiWithFilterGreaterThan() {
        Response response = RestAssured.given().log().all()
                .queryParam(GDP_FILTER, 5000)
                .queryParam(OPERATOR_FILTER, ">")
                .get(GET_COUNTRY_WITH_FILTER_API);

        //1. Verify status
        response.then().log().all().statusCode(200);
        //2. Verify headers
        response.then().header(X_POWERED_BY_HEADER, equalTo(X_POWERED_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(CONTENT_TYPE_HEADER_VALUE));
        //3. Verify body
        List<Country> actual = response.body().as(new TypeRef<>() {
        });
        for (Country country : actual) {
            assertThat(country.getGdp(), greaterThan(5000f));
        }
    }

    static Stream<Arguments> getCountryWithFilterProvider() {
        return Stream.of(
                Arguments.of(">", 5000, greaterThan(5000f)),
                Arguments.of(">=", 5000, greaterThanOrEqualTo(5000f)),
                Arguments.of("<", 5000, lessThan(5000f)),
                Arguments.of("<=", 5000, lessThanOrEqualTo(5000f)),
                Arguments.of("==", 5000, equalTo(5000f))
        );
    }

    @ParameterizedTest
    @MethodSource("getCountryWithFilterProvider")
    void verifyGetCountryApiWithFilter(String operator, int gdp, Matcher expected) {
        Response response = RestAssured.given().log().all()
                .queryParam(GDP_FILTER, gdp)
                .queryParam(OPERATOR_FILTER, operator)
                .get(GET_COUNTRY_WITH_FILTER_API);

        //1. Verify status
        response.then().log().all().statusCode(200);
        //2. Verify headers
        response.then().header(X_POWERED_BY_HEADER, equalTo(X_POWERED_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(CONTENT_TYPE_HEADER_VALUE));
        //3. Verify body
        List<Country> actual = response.body().as(new TypeRef<>() {
        });
        for (Country country : actual) {
            assertThat(country.getGdp(), expected);
        }
    }

}
