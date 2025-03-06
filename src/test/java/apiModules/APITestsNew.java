package apiModules;

import org.testng.Assert;
import org.testng.annotations.Test;
import  io.restassured.response.Response;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;


public class APITestsNew {

    @Test
    public void validarRetornoStatusCode(){

        Response response = get("https://reqres.in/api/users?page=2");
        int statusCode = response.getStatusCode();

        Assert.assertEquals(statusCode,200);
    }

    @Test
    public void validarRetornoStatusCodeRefactor(){
        baseURI = "https://reqres.in/api";
        given().
                get("/users?page=2").
                then().
                statusCode(200).
                body("data[1].id",equalTo(8));

    }

    @Test
    public void validarPrimeiroNome(){
        baseURI = "https://reqres.in/api";
        given().
                get("/users?page=2").
                then().
                statusCode(200).
                body("data[2].first_name",equalTo("Tobias")).
                body("data.first_name",hasItems("Byron","Tobias","Rachel"));

    }

}
