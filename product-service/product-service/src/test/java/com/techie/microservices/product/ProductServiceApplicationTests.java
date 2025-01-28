package com.techie.microservices.product;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.testcontainers.containers.MongoDBContainer;

@Import(TestcontainersConfiguration.class)

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)// to start the test on any random port
class ProductServiceApplicationTests {
	@ServiceConnection // injection of uri details for mongodb
	static MongoDBContainer mongoDBContainer =new MongoDBContainer("mongo:7.0.5");
	@LocalServerPort
	private Integer port; // injects the random port

	@BeforeEach
	void setup(){
		RestAssured.baseURI="http://localhost";
		RestAssured.port=port;
	}
	static {
		mongoDBContainer.start();
	}

	@Test
	void shouldCreateProduct() {
		String requestBody= """
				{
				    "name":"lamp",
				    "description":"lights up the room and your mood",
				    "price":100
				}
			""";

		RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/product")
				.then()
				.statusCode(201)
				.body("id", Matchers.notNullValue())
				.body("name",Matchers.equalTo("lamp"))
				.body("description",Matchers.equalTo("lights up the room and your mood"))
				.body("price",Matchers.equalTo(100));
	}

}