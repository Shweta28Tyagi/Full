package APITestCase;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import Util.LoginToken;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class BaseTest {
	
	static Response response;
	String token;
	
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
	   

}
