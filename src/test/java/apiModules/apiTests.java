package apiModules;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static io.restassured.RestAssured.*;

public class apiTests {
    private static int totalPages;
    private static int randomUserId;
    private static int newUserId;

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "https://reqres.in/api";
    }
    // Método para validar um único usuário
    private void validateSingleUser(Map<String, Object> user) {
        String email = user.get("email").toString();
        String firstName = user.get("first_name").toString();
        String lastName = user.get("last_name").toString();
        String avatar = user.get("avatar").toString();

        // Logar as características do usuário no console
        System.out.println("User Details:");
        System.out.println("Email: " + email);
        System.out.println("First Name: " + firstName);
        System.out.println("Last Name: " + lastName);
        System.out.println("Avatar: " + avatar);
        System.out.println("--------------------------");

        // Validar se os campos não estão vazios e não são nulos
        Assert.assertNotNull(email, "Email should not be null");
        Assert.assertFalse(email.isEmpty(), "Email should not be empty");

        Assert.assertNotNull(firstName, "First name should not be null");
        Assert.assertFalse(firstName.isEmpty(), "First name should not be empty");

        Assert.assertNotNull(lastName, "Last name should not be null");
        Assert.assertFalse(lastName.isEmpty(), "Last name should not be empty");

        Assert.assertNotNull(avatar, "Avatar should not be null");
        Assert.assertFalse(avatar.isEmpty(), "Avatar should not be empty");
    }
    // Método para validar uma lista de usuários
    private void validateListOfUsers(List<Object> users) {
        for (Object user : users) {
            // Acessar os campos diretamente do objeto do usuário
            String email = ((Map<String, Object>) user).get("email").toString();
            String firstName = ((Map<String, Object>) user).get("first_name").toString();
            String lastName = ((Map<String, Object>) user).get("last_name").toString();
            String avatar = ((Map<String, Object>) user).get("avatar").toString();

            // Logar as características do usuário no console
            System.out.println("User Details:");
            System.out.println("Email: " + email);
            System.out.println("First Name: " + firstName);
            System.out.println("Last Name: " + lastName);
            System.out.println("Avatar: " + avatar);
            System.out.println("--------------------------");

            // Verificando se os campos não estão vazios
            Assert.assertNotNull(email, "Email should not be null");
            Assert.assertFalse(email.isEmpty(), "Email should not be empty");

            Assert.assertNotNull(firstName, "First name should not be null");
            Assert.assertFalse(firstName.isEmpty(), "First name should not be empty");

            Assert.assertNotNull(lastName, "Last name should not be null");
            Assert.assertFalse(lastName.isEmpty(), "Last name should not be empty");

            Assert.assertNotNull(avatar, "Avatar should not be null");
            Assert.assertFalse(avatar.isEmpty(), "Avatar should not be empty");

        }
    }

    @Test
    public void listUsersAndStoreRandomId() {
        Response response = given()
                .when()
                .get("/users?page=1")
                .then()
                .statusCode(200)
                .extract().response();

        // Armazena o número total de páginas
        totalPages = response.jsonPath().getInt("total_pages");

        // Seleciona e armazena um ID de usuário aleatório
        int randomIndex = (int) (Math.random() * response.jsonPath().getList("data").size());
        randomUserId = (Integer) response.jsonPath().getList("data.id").get(randomIndex);

        // Valida as propriedades dos usuários
        validateListOfUsers(response.jsonPath().getList("data"));
    }


    @Test(dependsOnMethods = "listUsersAndStoreRandomId")
    public void validateSpecificUserWithRandomId() {
        Response response = given()
                .when()
                .get("/users/" + randomUserId)
                .then()
                .statusCode(200)
                .extract().response();

        // Validar as propriedades do usuário
        validateSingleUser(response.jsonPath().getMap("data"));
    }

    @Test(dependsOnMethods = "listUsersAndStoreRandomId")
    public void validateAllPagesOfUsers() {
        for (int page = 1; page <= totalPages; page++) {
            Response response = given()
                    .when()
                    .get("/users?page=" + page)
                    .then()
                    .statusCode(200)
                    .extract().response();

            // Validar as propriedades dos usuários
            validateListOfUsers(response.jsonPath().getList("data"));
        }
    }

    @Test
    public void returnError404WhenSearchingForANonExistentUser() {
        Response response = given()
                .when()
                .get("/users/23")
                .then()
                .statusCode(404)
                .extract().response();

        System.out.println("Response: " + response.getBody().asString());

        // Verifica se o corpo da resposta é um JSON vazio
        Assert.assertEquals(response.getBody().asString(), "{}", "Response body should be an empty JSON object");
    }
    @Test
    public void createAUserWithRandomNameAndJobTitle() {
        com.github.javafaker.Faker faker = new com.github.javafaker.Faker();
        String randomName = faker.name().firstName();
        String randomJob = faker.job().title();

        // Criar o corpo da requisição como um Map
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", randomName);
        requestBody.put("job", randomJob);

        // Logar os valores criados pelo Faker
        System.out.println("Creating user with:");
        System.out.println("Name: " + randomName);
        System.out.println("Job: " + randomJob);

        // Fazer a requisição POST
        Response response = given()
                .contentType("application/json")
                .body(requestBody) // Usar o mapa como corpo
                .when()
                .post("/users")
                .then()
                .statusCode(201) // Verificar se o status é 201
                .extract().response();

        // Logar os valores retornados na resposta
        System.out.println("Response received:");
        System.out.println("Name: " + response.jsonPath().getString("name"));
        System.out.println("Job: " + response.jsonPath().getString("job"));
        System.out.println("ID: " + response.jsonPath().getString("id"));

        // Armazenar o ID do usuário criado
        newUserId = response.jsonPath().getInt("id"); // Salvar o ID para uso posterior

        // Validar se a resposta é exatamente o que foi enviado
        Assert.assertEquals(randomName, response.jsonPath().getString("name"));
        Assert.assertEquals(randomJob, response.jsonPath().getString("job"));
        Assert.assertNotNull(newUserId, "ID should not be null"); // Verifica se o ID não é nulo
    }

    @Test(dependsOnMethods = "createAUserWithRandomNameAndJobTitle")
    public void updateUsersNameAndJobTitle() {
        int userId = newUserId; // Obtém o ID do usuário criado anteriormente

        String updatedName = "morpheus";
        String updatedJob = "zion resident";

        // Criar o corpo da requisição como um Map
        Map<String, Object> patchBody = new HashMap<>();
        patchBody.put("name", updatedName);
        patchBody.put("job", updatedJob);

        // Fazer a requisição PATCH
        Response patchResponse = given()
                .contentType("application/json")
                .body(patchBody) // Usar o mapa como corpo
                .when()
                .patch("/users/" + userId)
                .then()
                .statusCode(200) // Verificar se o status é 200
                .extract().response();

        System.out.println("Patch response:");
        System.out.println("Name: " + patchResponse.jsonPath().getString("name"));
        System.out.println("Job: " + patchResponse.jsonPath().getString("job"));

        // Validar a resposta
        Assert.assertEquals(updatedName, patchResponse.jsonPath().getString("name"));
        Assert.assertEquals(updatedJob, patchResponse.jsonPath().getString("job"));
    }
    @Test(dependsOnMethods = "updateUsersNameAndJobTitle")
    public void deleteTheCreatedAndUpdatedUser() {
        int userId = newUserId; // Obtém o ID do usuário criado anteriormente

        // Logar o ID do usuário que será excluído
        System.out.println("Deleting user with ID: " + userId);

        // Excluir o usuário
        Response deleteResponse = given()
                .when()
                .delete("/users/" + userId)
                .then()
                .statusCode(204) // Verifica se o status é 204
                .extract().response();

        // Logar o status da resposta da exclusão
        System.out.println("Delete response status: " + deleteResponse.getStatusCode());

        // Verificar se o usuário foi excluído
        Response getResponse = given()
                .when()
                .get("/users/" + userId)
                .then()
                .statusCode(404) // Verifica se o status é 404
                .extract().response();

        // Logar o status da resposta da tentativa de recuperação
        System.out.println("Get response status after deletion: " + getResponse.getStatusCode());

        // Verifica se o corpo da resposta está vazio
        Assert.assertEquals(getResponse.getBody().asString(), "{}", "Response body should be empty");
    }
}
