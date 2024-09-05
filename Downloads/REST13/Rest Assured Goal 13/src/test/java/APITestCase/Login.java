package APITestCase;

import org.hamcrest.core.Is;
import com.github.javafaker.Faker;
import com.relevantcodes.extentreports.LogStatus;
import com.reports.ExtentTestNGITestListener;

import Util.LoginData;
import Util.LoginToken;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.testng.Assert;
import static io.restassured.RestAssured.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.text.ParseException;

public class Login extends ExtentTestNGITestListener
{
   String token;
   String otpToken;
   int userID;
   String userName;
   String nameInCamelCase;
   Faker faker = new Faker();
   String newPassword;
   Response response;

   @BeforeClass
   public void setup() {
       RestAssured.baseURI = "https://mytyles.website:3133/api/v1";
       LoginToken.testLoginWithPassword();
       token=LoginToken.authToken;
   }

   @AfterClass
   public void close()
   {
 	  Assert.assertEquals(response.getStatusCode(), 200);
   }
   
   // Utility method to generate unique password
   private String generateUniquePassword() {
       String basePassword = "Test" + "@";
       Random random = new Random();
       int randomNumber = 100 + random.nextInt(900); // Generate a random number between 100 and 999
       return basePassword + randomNumber;
   }

   // Test case for login with password
   @Test
   public void LoginWithPassword() throws IOException {
       String file=LoginData.LoginWithPassword();
	  
        response = given()
               .contentType(ContentType.JSON)
               .body(file)
               .when()
               .post("/login")
               .then()
               .body("message", equalTo("Login successfully"))
               .extract().response();

        System.out.println("        ******** PRE LOGIN ********");
        
       String res = response.getBody().asString();
       System.out.println("Response of Login with Password : "+res);
       String authToken = JsonPath.from(res).get("data.token");
    
       String[] chunks = authToken.split("\\.");
       Base64.Decoder decoder = Base64.getUrlDecoder();
       String payload = new String(decoder.decode(chunks[1]));
       this.userID = JsonPath.from(payload).get("userId");
       int roleID = JsonPath.from(payload).get("roleId");
       userName = JsonPath.from(payload).get("firstName");
       nameInCamelCase = userName.substring(0, 1).toUpperCase() + userName.substring(1);

       Assert.assertNotNull(authToken, "Token should not be null");
       Assert.assertNotNull(userID, "User ID should not be null");
       Assert.assertNotNull(roleID, "Role ID should not be null");
   }

   // Test case for login with OTP
   @Test
   public void LoginWithOtp() throws IOException {
       String file=LoginData.LoginWithOtp();

        response = given()
               .contentType(ContentType.JSON)
               .body(file)
               .when()
               .post("/login")
               .then()
               .body("message", equalTo("OTP sent successfully"))
               .extract().response();

       System.out.println("Response of Login with Otp : "+response.getBody().asString());
   }

   // Test case for OTP verification
  @Test
   public void OtpVerification() throws IOException {
       String file = LoginData.OtpVerification();

        response = given()
               .contentType(ContentType.JSON)
               .body(file)
               .when()
               .post("/otpVerification")
               .then()
               .body("message", equalTo("Login successfully"))
               .extract().response();

       String res = response.getBody().asString();
       System.out.println("Response of Otp verification : "+res);
       try {
           JsonPath jsonPath = JsonPath.from(res);
           this.otpToken = jsonPath.getString("data.token");
       } catch (Exception e) {
           System.out.println("Failed to parse JSON response: " + e.getMessage());
       }

       if (res.contains("Otp must be 6 digit number")) {
           Assert.assertEquals(response.getStatusCode(), 403);
           response.then().assertThat().body("message", equalTo("Otp must be a 6 digit number"));
       } else if (res.contains("provided otp is not correct")) {
           Assert.assertEquals(response.getStatusCode(), 403);
           response.then().assertThat().body("message", equalTo("please register your account or provided otp is not correct"));
       } else if (res.contains("Login successfully")) {
           response.then().assertThat().body("message", equalTo("Login successfully"));
       } else {
           Assert.fail("Unexpected response: " + res);
       }
   }

   // Test case for resetting password
   @Test
   public void ResetPassword() throws IOException {
     String file=LoginData.ResetPassword();
    // System.out.println("Request JSON: " + file);
      
     response = given()
               .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token)
               .body(file)
               .when()
               .post("/resetPassword")
               .then()
               .body("message", equalTo("Password changed successfully"))
               .extract().response();

      System.out.println("Response of reset password : "+response.getBody().asString());
   }

   // Test case for checking reset password
   @Test(dependsOnMethods="ResetPassword")	
   public void TestResetPassword() throws IOException {
       String file = LoginData.CheckResetPassword();
        
       response = given()
               .contentType(ContentType.JSON)
               .body(file)
               .when()
               .post("/login")
               .then()
               .body("message", equalTo("Login successfully"))
               .extract().response();
       
       System.out.println("Response of check reset password : "+response.getBody().asString());
   }

   // Test case for fetching logged in user's profile
   @Test
   public void UserProfile() throws IOException {
       String file = LoginData.UserProfile();

        response = given()
               .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token)
               .body(file)
               .when()
               .post("/getUserProfile")
               .then()
               .body("message", equalTo("Record found successfully"))
               .extract().response();

       System.out.println("Response of logged in user : "+response.getBody().asString());
   }

   // Test case for logout
   @Test
   public void Logout() throws IOException {
       String payload = LoginData.Logout();
       
        response = given()
               .contentType(ContentType.JSON)
               .body(payload)
               .when()
               .post("/logout")
               .then()
               .body("message", equalTo("Logout successfully"))
               .extract().response();

       System.out.println("Response of Logout : "+response.getBody().asString());
   }
}