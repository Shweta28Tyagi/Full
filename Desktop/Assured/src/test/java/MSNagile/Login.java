package MSNagile;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import Util.LeadData;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.github.javafaker.Faker;

public class Login {
	Response response;
	static String authToken;
	String encEmail;
	String refreshToken;
	static Faker faker=new Faker();
	static String randomText = faker.lorem().characters(5, true, true);

	   @BeforeClass
	   public void setup() {
	       RestAssured.baseURI = "https://be.eazyagile.com";
	      
	   }

	   @AfterClass
	   public void close()
	   {
	 	  Assert.assertEquals(response.getStatusCode(), 200);
	   }
	   
	   // LOGIN
	   @Test
	   public void Login() throws IOException {
	       String file="{\r\n"
	       		+ "  \"url\": \"ss1\",\r\n"
	       		+ "  \"email\": \"shweta.tyagi+9@devstringx.com\",\r\n"
	       		+ "  \"password\": \"Shweta@123\",\r\n"
	       		+ "  \"gToken\": \"\",\r\n"
	       		+ "  \"type\": \"self\"\r\n"
	       		+ "}";
		  
	        response = given()
	               .contentType(ContentType.JSON)
	               .body(file)
	               .when()
	               .post("/login")
	               .then()
	               .body("message", equalTo("Loggedin Successfully"))
	               .extract().response();
	        
	       String res = response.getBody().asString();
	       System.out.println("Response of Login : "+res);
	       this.authToken = JsonPath.from(res).get("token");      
	    }
	   
	   //SIGNUP
	   @Test
	   public void Registration() throws IOException
	   {
		   String payload="{\r\n"
		   		+ "  \"url\": \""+randomText+"\",\r\n"
		   		+ "  \"fullName\": \"shikha\",\r\n"
		   		+ "  \"email\": \"shweta.tyagi+3@devstringx.com\",\r\n"
		   		+ "  \"password\": \"123456\",\r\n"
		   		+ "  \"couponCode\": \"\",\r\n"
		   		+ "  \"gToken\": \"\",\r\n"
		   		+ "  \"type\": \"self\"\r\n"
		   		+ "}";

	        response = given()
	        		.contentType(ContentType.JSON)
	               .header("Authorization", "Bearer " + authToken)
	               .body(payload)
	               .when()
	               .post("/registration")
	               .then()
	               .body("message",equalTo("Account created successfully, Please confirm the account by clicking the link we've just emailed you."))
	               .extract().response();
	       
	        String res=response.getBody().asString();
	        System.out.println("Response of registration : "+ res);
	   }
	   
	   //FORGOT PASSWORD
	   @Test
	   public void ForgotPassword()
	   {
		   String payload="{\r\n"
		   		+ "    \"url\": \"ss1\",\r\n"
		   		+ "    \"email\": \"shweta.tyagi+9@devstringx.com\"\r\n"
		   		+ "}";
		   
		   response=given()
				   .contentType(ContentType.JSON)
	               .body(payload)
	               .when()
	               .post("/forgotPassword")
	               .then()
	               .body("message", equalTo("If an account matches the details provided, an email will be sent to the registered email address with further instructions."))
	               .extract().response();
		   
		   String res=response.getBody().asString();
		   System.out.println("Response body of Forgot password : "+res);
		   
		   this.encEmail=JsonPath.from(res).get("encEmail");
		   
	   }
	   
	   //RESET PASSWORD
	   @Test
	   public void ResetPassword()
	   {
		   String payload="{\r\n"
		   		+ "    \"url\": \"ss1\",\r\n"
		   		+ "    \"encEmail\": \""+encEmail+"\",\r\n"
		   		+ "    \"newPassword\": \"Test@123\"\r\n"
		   		+ "}";
		   
		   response=given()
				   .contentType(ContentType.JSON)
				   .body(payload)
				   .when()
				   .post("/resetPassword")
				   .then()
				   .extract().response();
		   
		   String res=response.getBody().asString();
		   System.out.println("Response body of Reset password : "+res);
	   }
	   
	   //EMAIL VERIFICATION
	   @Test(dependsOnMethods="Login")
	   public void EmailVerification()
	   {
		   String payload="{\r\n"
		   		+ "  \"url\": \"ss1\"\r\n"
		   		+ "}";
		   
		   response=given()
				   .contentType(ContentType.JSON)
				   .header("Authorization", "Bearer " + authToken)
				   .body(payload)
				   .when()
				   .post("/auth/emailVerification")
				   .then()
				   .body("message", equalTo("Account verified successfully"))
				   .extract().response();
		   
		   String res=response.getBody().asString();
		   System.out.println("Response body of email verification : "+res);
	   }
	   
	   //REFRESH TOKEN
	   @Test(dependsOnMethods = "Login")
	   public void RefreshToken() {
	       
	       response = given()
	               .contentType(ContentType.JSON)
	               .header("Authorization", "Bearer " + authToken)
	               .when()
	               .post("/auth/refreshToken")
	               .then()
	               .body("message",equalTo("Token updated successfully"))
	               .extract().response();
	       
	       String res = response.getBody().asString();
	       System.out.println("Response body of refresh token: " + res); 
	       this.refreshToken=JsonPath.from(res).get("refresh_token");  
	   }
	 
	   //LOGOUT
	   @Test
	   public void Logout()
	   {
		   response=given()
				   .header("Authorization", "Bearer " + authToken)
				   .when()
				   .post("/auth/logout")
				   .then()
				   .body("message", equalTo("Logout Successfully"))
				   .extract().response();
		   
		   String res=response.getBody().asString();
		   System.out.println("Response body of logout : "+res);
	   }
	   
	   //Get profile
	   @Test(dependsOnMethods="Login")
	   public void GetProfile()
	   {
		   //System.out.println(authToken);
		  response=given()
				  .contentType(ContentType.JSON)
				  .header("Authorization", "Bearer " + authToken)
				  .when()
				  .get("/auth/getProfile")
				  .then()
				  .extract().response();
		  
		  String res=response.getBody().asString();
		  System.out.println("Response body of get profile : "+res); 
	   }   
}


