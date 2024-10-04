package MSNagile;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class SetupConfiguration {
	Response response;
    String authToken;

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "https://be.eazyagile.com";
    }

    @AfterClass
    public void close() {
        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 OK status code but received: " + response.getStatusCode());
    }

    @Test
    public void Login() throws IOException {
        String file = "{\r\n"
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
        System.out.println("Response of Login : " + res);
        this.authToken = JsonPath.from(res).get("token");
        Assert.assertNotNull(authToken, "Auth token should not be null after successful login.");
    }
    
    //Get system issue types
    @Test(dependsOnMethods="Login")
    public void GetSystemIssueType() throws IOException {

        response = given()
                .header("Authorization","Bearer "+authToken)
                .when()
                .get("/auth/getSystemIssuesTypes/ss1/null")
                .then()
                .body("message", equalTo("Successfully"))
                .extract().response();

        String res = response.getBody().asString();
        System.out.println("Response of get system issue types : " + res);
    }
    
    //Get system workflows
    @Test(dependsOnMethods="Login")
    public void GetSystemWorkflows() throws IOException {

        response = given()
                .header("Authorization","Bearer "+authToken)
                .when()
                .get("/auth/getSystemWorkflows/ss1")
                .then()
                .body("message", equalTo("Successfully"))
                .extract().response();

        String res = response.getBody().asString();
        System.out.println("Response of get system workflows : " + res);
    }

    //Get Organization fields
    @Test(dependsOnMethods="Login")
    public void GetOrgFields() throws IOException {

        response = given()
                .header("Authorization","Bearer "+authToken)
                .when()
                .get("/auth/getOrgFields/ss1")
                .then()
                .body("message", equalTo("Successfully"))
                .extract().response();

        String res = response.getBody().asString();
        System.out.println("Response of get org fields : " + res);
    }
    
    //Get all status
    @Test(dependsOnMethods="Login")
    public void GetAllStatus() throws IOException {

        response = given()
                .header("Authorization","Bearer "+authToken)
                .when()
                .get("/auth/getAllStatuses/ss1/null")
                .then()
                .body("message", equalTo("Successfully"))
                .extract().response();

        String res = response.getBody().asString();
        System.out.println("Response of get all statuses : " + res);
    }
    
    //Get organization tags
    @Test(dependsOnMethods="Login")
    public void GetOrgTags() throws IOException {

        response = given()
                .header("Authorization","Bearer "+authToken)
                .when()
                .get("/auth/getOrgTags/ss1")
                .then()
                .body("message", equalTo("Successfully"))
                .extract().response();

        String res = response.getBody().asString();
        System.out.println("Response of get org tags : " + res);
    }
    
    //Create issue type
    @Test(dependsOnMethods="Login")
    public void CreateIssueType() throws IOException {
    	String payload="{\r\n"
    			+ "    \"url\": \"ss1\",\r\n"
    			+ "    \"projectId\": \"null\",\r\n"
    			+ "    \"issueType\": \"Random\",\r\n"
    			+ "    \"color\": \"#2c8085\"\r\n"
    			+ "}";
    	
        response = given()
        		.contentType(ContentType.JSON)
                .header("Authorization","Bearer "+authToken)
                .body(payload)
                .when()
                .post("/auth/createIssueTypes")
                .then()
                .extract().response();

        String res = response.getBody().asString();
        //System.out.println("Response of create issue type : " + res);
        
        if (res.contains("IssueType Random created successfully")) {
	         System.out.println("Issue type created successfully");
	     } else {
	         // Handle other responses or successful invite
	         System.out.println("Issue Type name already exist");
	         Assert.assertEquals(response.getStatusCode(), 208);  //208 already reported status code
	     }
    }
   
    //Delete custom issue type
    @Test(dependsOnMethods="Login")
    public void DeleteCustomIssueType() throws IOException {

    	String payload="{\r\n"
    			+ "    \"url\": \"ss1\",\r\n"
    			+ "    \"projectId\": \"null\",\r\n"
    			+ "    \"issueTypeId\": 90\r\n"
    			+ "}";
    	
        response = given()
        		.contentType(ContentType.JSON)
                .header("Authorization","Bearer "+authToken)
                .body(payload)
                .when()
                .post("/auth/deleteCustomIssueTypes")
                .then()
                .body("message", equalTo("Successfully"))
                .extract().response();

        String res = response.getBody().asString();
        System.out.println("Response of delete custom issue type : " + res);
    }
    
    //Create status
    @Test(dependsOnMethods="Login")
    public void CreateStatus()
    {
    	String payload="{\r\n"
    			+ "  \"url\": \"ss1\",\r\n"
    			+ "  \"projectId\": \"null\",\r\n"
    			+ "  \"statusName\": \"input\",\r\n"
    			+ "  \"color\": \"#c6c6c7\",\r\n"
    			+ "  \"category\": \"ToDo\"\r\n"
    			+ "}";
    	
    	response=given()
    			.contentType(ContentType.JSON)
    			.header("Authorization","Bearer "+authToken)
    			.body(payload)
    			.when()
    			.post("/auth/createStatus")
    			.then()
    			.extract().response();
    	
    	String res=response.getBody().asString();
    	
    	if(res.contains("status created successfully!"))
    	{
    		System.out.println("Response of create status : "+res);
    	}
    	else
    	{
    		System.out.println("Status name already exist");
    		Assert.assertEquals(response.getStatusCode(), 208);
    	}
    }
    
    //Delete status
    @Test(dependsOnMethods="Login")
    public void DeleteStatus()
    {
    	String payload="{\r\n"
    			+ "    \"url\": \"ss1\",\r\n"
    			+ "    \"statusId\": 248,\r\n"
    			+ "    \"projectId\": \"null\"\r\n"
    			+ "}";
    	
    	response=given()
    			.contentType(ContentType.JSON)
    			.headers("Authorization","Bearer "+authToken)
    			.body(payload)
    			.when()
    			.post("/auth/deleteCustomStatus")
    			.then()
    			.extract().response();
    	
    	String res=response.getBody().asString();
    	
    	if(res.contains("Status deleted  successfully!"))
    	{
    		System.out.println("Response of delete status : "+res);
    	}
    	else
    	{
    		System.out.println("Incorrect status id or already status deleted with this id");
    	}
    }
    
    //Create Fields
    @Test(dependsOnMethods="Login")
    public void CreateFields()
    {
    	response = given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + authToken)
                    .when()
                    .get("/auth/getActivePlan/ss1")
                    .then()
                    .statusCode(200)
                    .body("message", equalTo("Successfully"))
                    .extract().response();

            String res = response.getBody().asString();
            System.out.println("Response of Get active plan : " + res);

            String planName = JsonPath.from(res).get("data.planName");
           // System.out.println(planName);
            Assert.assertNotNull(planName, "Active plan name should not be null.");
        }   
}

