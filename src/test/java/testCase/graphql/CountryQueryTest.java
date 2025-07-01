package testCase.graphql;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.dto.graphql.QueryRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.IFileUtils;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static utils.ConstantUtils.CONTENT_TYPE_HEADER;
import static utils.ConstantUtils.REQUEST_CONTENT_TYPE_HEADER_VALUE;

public class CountryQueryTest {
    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "https://countries.trevorblades.com";
        RestAssured.port = 443;
    }

    @Test
    void verifyCountryQuery() throws IOException {
        String queryFilePath = "graphql/graphql-query/country-query.graphql";
        String queryString = IFileUtils.readFileFromResources(queryFilePath);
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setQuery(queryString);
        Map<String, String> variables = new HashMap<>();
        variables.put("code", "VN");
        queryRequest.setVariables(variables);
        Response response = RestAssured.given().log().all()
                .header(CONTENT_TYPE_HEADER, REQUEST_CONTENT_TYPE_HEADER_VALUE)
                .body(queryRequest)
                .post();
        //Schema
        //Status code
        response.then().log().all().statusCode(200);
        //Headers
        response.then().header(CONTENT_TYPE_HEADER, "application/json; charset=utf-8");
        //Body
        String actual = response.body().asString();
        String expectedPath = "graphql/expected/countryQueryExpected.json";
        String expected = IFileUtils.readFileFromResources(expectedPath);
        assertThat(actual, jsonEquals(expected));
    }
}
