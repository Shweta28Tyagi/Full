package APITestCase;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.io.FileUtils;  //IMAGE UPLOAD
import org.hamcrest.Matchers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.github.javafaker.Faker;

import Util.LeadData;
import Util.LoginToken;
import Util.QuotationData;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapper;
import io.restassured.matcher.ResponseAwareMatcher;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import static org.hamcrest.Matchers.hasItem;


public class ManageLead {
	String token;
    public static int leadId;
    static Response response;
    int lead_owner;
    public static String currentStage;
   
    @BeforeClass
    public void setup()
    {
    	RestAssured.baseURI = "https://mytyles.website:3133/api/v1";
    	LoginToken.testLoginWithPassword();
	    token=LoginToken.authToken;
    }
    @AfterClass
    public static void close() {
    	Assert.assertEquals(response.getStatusCode(), 200);
    }
      
     //Get lead owners list
     @Test
     public void GetLeadOwner() throws IOException
     {
    	 Map<String, Object> data=LeadData.GetLeadOwner();
    	 
          response = RestAssured.given()
                 .contentType(ContentType.JSON)
                 .header("Authorization", "Bearer " + token) // Pass token in the header
                 .body(data)
                 .when()
                 .post("/getUsers")
                 .andReturn();
         String exp = response.getBody().asString();
         System.out.println("Response body of get lead owners : " + exp);

         List<Integer> SalesExecutiveId = JsonPath.from(exp).get("data.records.id");
         System.out.println("Sales excutives : "+SalesExecutiveId);
         this.lead_owner=SalesExecutiveId.get(0);
         
         response.then().assertThat().body("message", equalTo("Record found successfully"));
     }
     
    //ADD LEAD
    @Test
    public void AddLead() throws IOException
    {   
 	  Map<String, Object> data=LeadData.AddLead();
 	  
    	response = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token) // Pass token in the header
                .body(data)
                .when()
                .post("/createLead")
                .andReturn();
        String exp = response.getBody().asString();
        System.out.println("Response body of add lead : " + exp);
        this.leadId = JsonPath.from(exp).get("data.lead_id");
        System.out.println("Added lead Id : " + leadId);
    }
         
    // GET LEADS
   @Test(dependsOnMethods="AddLead")
    public void GetLeads() throws IOException
    {
        String file=LeadData.GetLeads();
        
         response = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token) // Pass token in the header
                .body(file)
                .when()
                .post("/getLeads")
                .andReturn();
         
        String exp = response.getBody().asString();
        System.out.println("Response body of get lead : " + exp);

        int count = JsonPath.from(exp).get("data.count");
        System.out.println("Total lead count : "+count);
              
     // Extract list of user IDs from the response
        List<Integer> leadIds = response.jsonPath().getList("data.records.lead_id");

        // Step 3: Assert that the user ID exists in the list of all users
        Assert.assertTrue(leadIds.contains(leadId), "Lead ID " + leadId + " not found in the list of users!");      
    }
   
   // Verify added user
   		//@Test
	    public void addUserAndVerifyInGetAllLeads() {
	        // Step 1: Add a new user and capture the user ID
	   Map<String, Object> data=LeadData.AddLead();
	   
	        // POST request to add user
	        response = RestAssured
	                .given()
	                .contentType(ContentType.JSON)
	                .header("Authorization", "Bearer " + token)
	                .body(data)
	                .when()
	                .post("/createLead")
	                .then() 
	                .extract()
	                .response();

	        //System.out.println("error " + response.jsonPath().getInt("data.lead_id"));
	        // Extract user ID from the response
	        String exp = response.getBody().asString();
	        System.out.println("Response body of add lead : " + exp);
	        
	        Integer leadIdNew = response.jsonPath().get("data.lead_id");
	        System.out.println("Lead ID created: " + leadIdNew); 
	        if (leadIdNew == null) {
	            System.out.println("Lead ID not found in the response!");
	        } else {
	            System.out.println("Lead ID created: " + leadIdNew);
	        }
	        
	        // Step 2: Get all users and check if the newly added user exists
	        String file=LeadData.GetLeads();
	        
	        response = RestAssured
	                .given()
	                .contentType(ContentType.JSON)
	                .header("Authorization", "Bearer " + token)
	                .body(file)
	                .when()
	                .post("/getLeads")
	                .then()
	                .extract()
	                .response();
	        
	        String exp1 = response.getBody().asString();
	        System.out.println("Response body of get lead : " + exp1);

	        // Extract list of user IDs from the response
	        List<Integer> leadIds = response.jsonPath().getList("data.records.lead_id");
	        System.out.println(leadIds);

	        // Step 3: Assert that the user ID exists in the list of all users
	        Assert.assertTrue(leadIds.contains(leadIdNew), "Lead ID " + leadIdNew + " not found in the list of users!");
	    }
	
   //UPDATE LEAD
   @Test
   public void UpdateLead() throws IOException
   {
	   Map<String, Object> data=LeadData.UpdateLead();
       
        response = RestAssured.given()
               .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token) // Pass token in the header
               .body(data)
               .when()
               .post("/updateLead")
               .then()
               .body("message",equalTo("Lead updated successfully."))
               .extract().response();
        
       String exp = response.getBody().asString();  
       System.out.println("Respons ebody of update lead : "+exp);
   }
   
   //ADD ATTACHMENTS
   @Test
   public void UploadLeadAttachment() throws IOException
   {
	   String[] filePaths = LeadData.AddLeadAttachment();

	    File file = new File(filePaths[0]);

       // Construct the multipart form data request
        response = given()
               .header("Authorization", "Bearer " + token)
               .basePath("/addUploadMultipleAttachments")
               .multiPart("id", 104) // Replace with actual id
               .multiPart("file", file, "image/jpeg")
               .header("Accept", "*/*")
               .header("Content-Type", "multipart/form-data")
               .when()
               .post();

       // Parse the response body as JSON
       JSONObject responseBody = new JSONObject(response.getBody().asString());
       System.out.println("Response body of lead attachment : " + responseBody.toString(4));  
   }
   
   //EXPORT LEADS
   @Test
   public void ExportLeads() throws IOException
   {
	   String file=LeadData.ExportLead();
	   
        response = given()
    		   .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token)
               .body(file)
               .when()
               .post("/exportLeads");

       // Parse the response body as JSON
       JSONObject responseBody = new JSONObject(response.getBody().asString());
       System.out.println("Response body of export lead : " + responseBody.toString(4));
   }
   
   //Add task
   @Test
   public void AddTaskForLead() throws IOException
   {
	   Map<String, Object> payload=LeadData.AddLeadTask();
	   
        response = given()
    		   .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token)
               .body(payload)
               .when()
               .post("/createLeadTask");

       // Parse the response body as JSON
       JSONObject responseBody = new JSONObject(response.getBody().asString());
       System.out.println("Response body add task : " + responseBody);
   }
   
   //GET LEAD TASK
   @Test
   public void GetLeadTask() throws IOException
   {	  
	   String file=LeadData.GetLeadTask();
	   
        response = given()
    		   .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token)
               .body(file)
               .when()
               .post("/getLeadTask");

       // Parse the response body as JSON
       JSONObject responseBody = new JSONObject(response.getBody().asString());
       System.out.println("Response body of all lead task : " + responseBody);
   }
   
   //GET LEAD HISTORY
   @Test
   public void GetLeadHistory() throws IOException
   {
	   Map<String, Object> payload=LeadData.GetLeadHistory();
	   
	   response = given()
    		   .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token)
               .body(payload)
               .when()
               .post("/getLeadHistory");

       // Parse the response body as JSON
       JSONObject responseBody = new JSONObject(response.getBody().asString());
       System.out.println("Response body of lead history : " + responseBody);
   }
   
   //ADD LEAD ACTIVITY
   @Test
   public void AddLeadActivity() throws IOException
   {
	   Map<String, Object> payload=LeadData.AddLeadActivity();

	    response = given()
    		   .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token)
               .body(payload)
               .when()
               .post("/createLeadActivity");

       // Parse the response body as JSON
       JSONObject responseBody = new JSONObject(response.getBody().asString());
       System.out.println("Response body of add lead activity : " + responseBody);
   }
   
   //GET ACTIVITY
   @Test
   public void GetLeadActivity() throws IOException
   {
	    response = given()
    		   .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token)
               .when()
               .post("/getActivityType");

       // Parse the response body as JSON
       JSONObject responseBody = new JSONObject(response.getBody().asString());
       System.out.println("Response body of get lead activities : " + responseBody);
   }
   
   //CITIES
   @Test
   public void GetCity() throws IOException
   {
	  Map<String, Object> payload=LeadData.GetCity();
	 
	    response = given()
    		   .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token)
               .body(payload)
               .when()
               .post("/getCities");

       // Parse the response body as JSON
       JSONObject responseBody = new JSONObject(response.getBody().asString());
       System.out.println("Response body of get cities : " + responseBody);
   }
   
   //ALL STATES
   @Test
   public void GetStates() throws IOException
   {
	    response = given()
    		   .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token)
               .when()
               .post("/getStates");

       // Parse the response body as JSON
       JSONObject responseBody = new JSONObject(response.getBody().asString());
       System.out.println("Response body of get states : " + responseBody);
   }
   
   //GET LEAD DETAILS
   @Test
   public void GetLeadDetails() throws IOException
   {
	   Map<String, Object> payload=LeadData.getLeadDetails();
	   
	    response = given()
    		   .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token)
               .body(payload)
               .when()
               .post("/getLeadDetails");

       // Parse the response body as JSON
       JSONObject responseBody = new JSONObject(response.getBody().asString());
       System.out.println("Response body of lead details : " + responseBody);
   }
   
   //CHECK LEAD NUMBERS
   @Test
   public void CheckLeadNumbers() throws IOException
   {
	   Map<String, Object> payload=LeadData.CheckLeadNumbers();
	   
	    response = given()
    		   .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token)
               .body(payload)
               .when()
               .post("/checkLeadNumbers");

       // Parse the response body as JSON
       JSONObject responseBody = new JSONObject(response.getBody().asString());
       System.out.println("Response body of checking lead numbers : " + responseBody);
   }
   
   //UPDATE LEAD STAGE   
   @Test(dependsOnMethods="AddLead")
   public void UpdateLeadStage() throws IOException {
       String file=LeadData.UpdateLeadStage();
       String[] stages = {"New Lead", "Follow Up", "Showroom Visit", "Quotation", "Order Placed", "General Enquiry", "Lost", "Invalid"};
	      
       // Fetch the current lead stage
       response = given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", "Bearer " + token)
                  .body(file)
                  .when()
                  .post("/getLeadDetails")
                  .andReturn();
       
       // Log the raw response for debugging
       String rawResponse = response.getBody().asString();
       System.out.println("Response of get lead details: " + rawResponse);

       // Use JsonPath to safely extract the data
       try {
           Map<String, Object> responseMap = JsonPath.from(rawResponse).getMap("data.record");

           if (responseMap == null) {
               throw new IOException("Record key not found in response: " + rawResponse);
           }

           // Extract the current lead stage
           currentStage = (String) responseMap.get("lead_stage");
           System.out.println("Current lead stage: " + currentStage);

           // Select a new stage different from the current stage
           String newStage = stages[0];
           if (newStage.equals(currentStage)) {
               newStage = stages[1];  // Choose a different stage if they are the same
           }

           Map<String, Object> payload = LeadData.UpdateLeadStage1();

           response = given()
                      .contentType(ContentType.JSON)
                      .header("Authorization", "Bearer " + token)
                      .body(payload)
                      .when()
                      .post("/updateLeadStage");

           // Log the raw response for debugging
           rawResponse = response.getBody().asString();
           System.out.println("Response of update lead stage: " + rawResponse);

           // Check if the response is valid JSON
           JSONObject responseBody;
           try {
               responseBody = new JSONObject(rawResponse);
               System.out.println("Response body of update lead stage: " + responseBody.toString(2));
           } catch (JSONException e) {
               throw new IOException("Invalid JSON response: " + rawResponse, e);
           }
       } catch (Exception e) {
           throw new IOException("Error processing response: " + rawResponse, e);
       }
   }
   
   //GET LEAD STAGE  
   @Test
   public void GetLeadStage() throws IOException
   {
	    response = given()
    		   .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token)
               .when()
               .post("/getLeadStage");

       // Parse the response body as JSON
       String res = response.getBody().asString();
       System.out.println("Response body of get lead stage : " + res);
   }
   
   //GET REQUIREMENTS
   @Test
   public void GetRequirement() throws IOException
   {
	    response = given()
    		   .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token)
               .when()
               .post("/getRequirements");

       // Parse the response body as JSON
       JSONObject responseBody = new JSONObject(response.getBody().asString());
       System.out.println("Response body of get requirements : " + responseBody);
   }
   
   //MARK LEAD AS STAR
   @Test
   public void MarkLeadAsStar() throws IOException
   {
	   String file=LeadData.MarkLeadAsStar();
	   
	   response= given()
    		   .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token)
               .body(file)
               .when()
               .post("/markLeadAsStar");

       // Parse the response body as JSON
       JSONObject responseBody = new JSONObject(response.getBody().asString());
       System.out.println("Response body of mark lead as star : " + responseBody);
   }
   
   //GET LEAD TASK BY ID
   @Test
   public void GetTaskByLeadId() throws IOException
   {
	 String file=LeadData.GetTaskByLeadId();
	   
	    response = given()
    		   .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token)
               .body(file)
               .when()
               .post("/getLeadTaskByLeadId");

       // Parse the response body as JSON
       JSONObject responseBody = new JSONObject(response.getBody().asString());
       System.out.println("Response body of  get lead task by id : " + responseBody);
   }
   
   //Mark Task as Completed
   @Test
   public void MarkTaskAsCompleted() throws IOException
   {
	  String file=LeadData.MarkTaskAsCompleted();
	   
	    response = given()
    		   .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token)
               .body(file)
               .when()
               .post("/markTaskAsCompleted");

       // Parse the response body as JSON
       JSONObject responseBody = new JSONObject(response.getBody().asString());
       System.out.println("Response body of mark task as completed : " + responseBody);
   }
   
   //RESCHEDULE TASK
   @Test
   public void RescheduleTask() throws IOException
   {
	   Map<String, Object> payload = LeadData.RescheduleTask();
	   
	    response = given()
    		   .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token)
               .body(payload)
               .when()
               .post("/rescheduleTask");

       // Parse the response body as JSON
       JSONObject responseBody = new JSONObject(response.getBody().asString());
       System.out.println("Response body reschedule task : " + responseBody);
   }
   
   //DELETE LEAD ATTACHMENT
   @Test
   public void DeleteAttachment() throws IOException
   {
	   String file=LeadData.DeleteAttachment();
	   
	    response = given()
    		   .contentType(ContentType.JSON)
               .header("Authorization", "Bearer " + token)
               .body(file)
               .when()
               .post("/deleteAttachments");

       // Parse the response body as JSON
       JSONObject responseBody = new JSONObject(response.getBody().asString());
       System.out.println("Response body delete attachment : " + responseBody);

       int statusCode = response.getStatusCode();
       if (statusCode == 200) {
           System.out.println("Test passed.");
       } else {
           System.out.println("Test failed with status code: " + statusCode);
       }
   }
}