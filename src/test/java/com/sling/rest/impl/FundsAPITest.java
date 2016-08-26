package com.sling.rest.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.config.EncoderConfig;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.sling.rest.RestVerticle;
import com.sling.rest.persist.MongoCRUD;
import com.sling.rest.resource.utils.NetworkUtils;

import static com.jayway.restassured.http.ContentType.JSON;
import static com.jayway.restassured.http.ContentType.TEXT;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyArray;

public class FundsAPITest {
  private static Vertx vertx;
  
  /** funds path */
  private static String funds    = "/apis/funds";
  /** invoices path */
  private static String invoices = "/apis/invoices";


  private static void startEmbeddedMongo() throws Exception {
    MongoCRUD.setIsEmbedded(true);
    MongoCRUD.getInstance(vertx).startEmbeddedMongo();
  }

  private static void deployRestVerticle() {
    DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(
        new JsonObject().put("http.port", RestAssured.port));
    vertx.deployVerticle(RestVerticle.class.getName(), deploymentOptions);
  }
  
  @BeforeClass
  public static void beforeClass() throws Exception {
    vertx = Vertx.vertx();
    RestAssured.port = NetworkUtils.nextFreePort(); 
    RestAssured.baseURI = "http://localhost";
    RestAssured.config = RestAssured.config().encoderConfig(EncoderConfig.encoderConfig()
        .appendDefaultContentCharsetToContentTypeIfUndefined(false));
    RestAssured.requestSpecification = new RequestSpecBuilder()
      .addHeader("Authorization", "authtoken")
      .build();

    startEmbeddedMongo();
    deployRestVerticle();
  }

  private String getFile(String filename) throws IOException {
    return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(filename), "UTF-8");
  }
  
  @Test
  public void getFunds() throws IOException {
    given().accept(TEXT).
    when().get(funds).
    then().
      body("total_records", equalTo(0)).
      body("funds", empty());
    
    Response response =
    given().
      body(getFile("fund1.json")).
      contentType(JSON).
      accept("text/plain; charset=ISO-8859-1").
    when().
      post(funds).
    then().
      statusCode(201).
    extract().
      response();
    System.out.println(response.asString());

    response =
    given().accept(TEXT).
    when().get(funds).
    then().
      body("total_records", equalTo(1)).
      body("funds[0].code", equalTo("MEDGRANT")).
    extract().response();
    System.out.println(response.asString());
  }

  @Test
  public void getInvoices() {
    given().accept(TEXT).
    when().get(invoices).
    then().
      body("total_records", equalTo(0)).
      body("invoices", empty());
  }
}
