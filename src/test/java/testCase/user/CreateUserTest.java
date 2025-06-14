package testCase.user;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.user.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import testCase.MasterTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static utils.ConstantUtils.*;
import static utils.DateTimeUtils.parseTimeToCurrentTimeZone;
import static utils.DateTimeUtils.verifyDateTime;

public class CreateUserTest extends MasterTest {
    private static final String[] IGNORE_FIELDS = {"id", "createdAt", "updatedAt", "addresses[*].id", "addresses[*].customerId",
            "addresses[*].createdAt", "addresses[*].updatedAt"};
    private static final String EMAIL_TEMPLATE = "auto_api_%s@abc.com";
    private static List<String> ids = new ArrayList<>();

    @AfterAll
    static void tearDown() {
        for (String id : ids) {
            RestAssured.given().log().all()
                    .header(CONTENT_TYPE_HEADER, REQUEST_CONTENT_TYPE_HEADER_VALUE)
                    .header(AUTHORIZATION_HEADER, token)
                    .delete(DELETE_USER_API, id)
                    .then()
                    .log()
                    .all();
        }
    }

    @Test
    void verifyCreateUserSchema() {
        //2. Verify schema
    }


    @Test
    void verifyCreateUserSuccessful() {
        //Create UserAddress
        UserAddressRequest userAddressRequest = UserAddressRequest.getDefault();
        //Create user
        UserRequest userRequest = UserRequest.getDefault();
        userRequest.setEmail(String.format(EMAIL_TEMPLATE, System.currentTimeMillis()));
        userRequest.setAddresses(List.of(userAddressRequest));

        LocalDateTime timeBeforeCreate = LocalDateTime.now();

        Response createUserResponse = createUser(userRequest);
        //1. Verify status code
        createUserResponse.then().log().all().statusCode(200);
        //3. Verify headers
        createUserResponse.then().header(X_POWERED_BY_HEADER, equalTo(X_POWERED_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(RESPONSE_CONTENT_TYPE_HEADER_VALUE));
        //4. Verify body
        UserResponse userResponse = createUserResponse.body().as(UserResponse.class);
        assertThat(userResponse.getId(), not(emptyOrNullString()));
        assertThat(userResponse.getMessage(), equalTo("Customer created"));
        ids.add(userResponse.getId());
        //5. Double check that user existing in the system or not by getUserApi
        Response getUserResponse = getUser(userResponse);
        LocalDateTime timeAfterCreate = LocalDateTime.now();
        //6. Verify status
        getUserResponse.then().log().all().statusCode(200);
        //7. Verify get user response again request
        GetUserResponse actualGetUserResponse = getUserResponse.body().as(GetUserResponse.class);
        assertThat(actualGetUserResponse.getId(), equalTo(userResponse.getId()));
        assertThat(actualGetUserResponse, jsonEquals(userRequest)
                .whenIgnoringPaths(IGNORE_FIELDS));

        LocalDateTime userCreateAtDate = parseTimeToCurrentTimeZone(actualGetUserResponse.getCreatedAt());
        verifyDateTime(timeBeforeCreate, timeAfterCreate, userCreateAtDate);

        LocalDateTime userUpdateAtDate = parseTimeToCurrentTimeZone(actualGetUserResponse.getUpdatedAt());
        verifyDateTime(timeBeforeCreate, timeAfterCreate, userUpdateAtDate);

        for (GetUserAddressResponse addressResponse : actualGetUserResponse.getAddresses()) {
            assertThat(addressResponse.getCustomerId(), equalTo(userResponse.getId()));
            assertThat(addressResponse.getId(), not(emptyOrNullString()));
            LocalDateTime userAddressCreateAtDate = parseTimeToCurrentTimeZone(addressResponse.getCreatedAt());
            verifyDateTime(timeBeforeCreate, timeAfterCreate, userAddressCreateAtDate);
            LocalDateTime userAddressUpdateAtDate = parseTimeToCurrentTimeZone(addressResponse.getCreatedAt());
            verifyDateTime(timeBeforeCreate, timeAfterCreate, userAddressUpdateAtDate);
        }
    }

    @Test
    void verifyCreateUserSuccessfulWithMultipleAddress() {
        //Create UserAddress
        UserAddressRequest userAddressRequest1 = UserAddressRequest.getDefault();
        UserAddressRequest userAddressRequest2 = UserAddressRequest.getDefault();
        userAddressRequest2.setStreetNumber("456");
        //Create user
        UserRequest userRequest = UserRequest.getDefault();
        userRequest.setEmail(String.format(EMAIL_TEMPLATE, System.currentTimeMillis()));
        userRequest.setAddresses(List.of(userAddressRequest1, userAddressRequest2));
        LocalDateTime timeBeforeCreate = LocalDateTime.now();

        Response createUserResponse = createUser(userRequest);
        //1. Verify status code
        createUserResponse.then().log().all().statusCode(200);
        //3. Verify headers
        createUserResponse.then().header(X_POWERED_BY_HEADER, equalTo(X_POWERED_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(RESPONSE_CONTENT_TYPE_HEADER_VALUE));
        //4. Verify body
        UserResponse userResponse = createUserResponse.body().as(UserResponse.class);
        assertThat(userResponse.getId(), not(emptyOrNullString()));
        assertThat(userResponse.getMessage(), equalTo("Customer created"));
        ids.add(userResponse.getId());

        //5. Double check that user existing in the system or not by getUserApi
        Response getUserResponse = getUser(userResponse);
        LocalDateTime timeAfterCreate = LocalDateTime.now();
        //6. Verify status
        getUserResponse.then().log().all().statusCode(200);
        //7. Verify get user response again request
        GetUserResponse actualGetUserResponse = getUserResponse.body().as(GetUserResponse.class);
        assertThat(actualGetUserResponse.getId(), equalTo(userResponse.getId()));
        assertThat(actualGetUserResponse, jsonEquals(userRequest)
                .whenIgnoringPaths(IGNORE_FIELDS));

        LocalDateTime userCreateAtDate = parseTimeToCurrentTimeZone(actualGetUserResponse.getCreatedAt());
        verifyDateTime(timeBeforeCreate, timeAfterCreate, userCreateAtDate);

        LocalDateTime userUpdateAtDate = parseTimeToCurrentTimeZone(actualGetUserResponse.getUpdatedAt());
        verifyDateTime(timeBeforeCreate, timeAfterCreate, userUpdateAtDate);

        for (GetUserAddressResponse addressResponse : actualGetUserResponse.getAddresses()) {
            assertThat(addressResponse.getCustomerId(), equalTo(userResponse.getId()));
            assertThat(addressResponse.getId(), not(emptyOrNullString()));
            LocalDateTime userAddressCreateAtDate = parseTimeToCurrentTimeZone(addressResponse.getCreatedAt());
            verifyDateTime(timeBeforeCreate, timeAfterCreate, userAddressCreateAtDate);
            LocalDateTime userAddressUpdateAtDate = parseTimeToCurrentTimeZone(addressResponse.getCreatedAt());
            verifyDateTime(timeBeforeCreate, timeAfterCreate, userAddressUpdateAtDate);
        }
    }

    private static Response getUser(UserResponse userResponse) {
        return RestAssured.given().log().all()
                .header(CONTENT_TYPE_HEADER, REQUEST_CONTENT_TYPE_HEADER_VALUE)
                .header(AUTHORIZATION_HEADER, token)
                .get(GET_USER_API, userResponse.getId());
    }

    private static Response createUser(UserRequest userRequest) {
        return RestAssured.given().log().all()
                .header(CONTENT_TYPE_HEADER, REQUEST_CONTENT_TYPE_HEADER_VALUE)
                .header(AUTHORIZATION_HEADER, token)
                .body(userRequest)
                .post(CREATE_USER_API);
    }
}
