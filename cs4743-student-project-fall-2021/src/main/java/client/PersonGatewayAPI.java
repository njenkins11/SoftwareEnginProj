package client;

import exceptions.PersonException;
import exceptions.UnauthorizedException;
import exceptions.UnknownException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mvc.model.Audit;
import mvc.model.FetchResults;
import mvc.model.Person;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.json.JSONArray;
import org.json.JSONObject;
import wiremock.org.checkerframework.checker.units.qual.A;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class PersonGatewayAPI {
    private final String wsURL;
    private final String sessionId;
    private final int DEFAULT_SIZE = 10;
    private static final Logger LOGGER = LogManager.getLogger();

    public PersonGatewayAPI(String url, String sessionId){
        this.sessionId = sessionId;
        wsURL = url;
    }

    /**
     * Gets the array list of people from Wiremock
     * Arraylist could be empty if an exception has occurred.
     * @return Returns an arraylist of people
     */
    public FetchResults fetchPersons(int page, String search){
        ArrayList<Person> personList = new ArrayList<>();
        FetchResults fetchResults;

        try{
            HttpGet request = new HttpGet(wsURL+"?page="+page+"&size="+DEFAULT_SIZE+"&keyword="+search);
            request.setHeader("authorization", sessionId);
            JSONObject credentials = new JSONObject();


            String response = waitForResponseAsString(request);
            JSONObject root = new JSONObject(response);
            int pageSize = root.getInt("pageSize");
            int currentPage = root.getInt("currentPage");
            long maxElements = root.getLong("maxElements");


            for(Object obj : new JSONArray(root.getJSONArray("persons").toString())){
                JSONObject jsonObject = (JSONObject) obj;
                // Sets up date of birth according to Wiremock / http connection
                LocalDate dateOfBirth;
                dateOfBirth = LocalDate.parse(jsonObject.getString("dateOfBirth"));
                ZonedDateTime currentTimeStamp;
                currentTimeStamp = ZonedDateTime.parse(jsonObject.getString("currentTimeStamp"));
                // Creates a person with the given information. Can cause parsing errors, however.
                Person person = new Person(jsonObject.getInt("id"),jsonObject.getString("firstName"), jsonObject.getString("lastName"),dateOfBirth, currentTimeStamp);
                person.setId(jsonObject.getInt("id"));
                personList.add(person);
            }
            fetchResults = new FetchResults(pageSize,currentPage,maxElements,personList);
            }catch(Exception e){
                LOGGER.error(e);
                throw new PersonException(e);
        }

        return fetchResults;
    }

    public ArrayList<Audit> fetchAuditByPersonId(long personId){
        ArrayList<Audit> auditList = new ArrayList<>();
        try{
            HttpGet request = new HttpGet(wsURL + "/" + personId +"/audittrail");
            request.setHeader("authorization", sessionId);
            CloseableHttpClient httpclient = HttpClients.createDefault();
            CloseableHttpResponse response = httpclient.execute(request);
            if(response.getStatusLine().getStatusCode() == 200) {
                String responses = parseResponseToString(response);
                for (Object obj : new JSONArray(responses)) {
                    JSONObject jsonObject = (JSONObject) obj;
                    Audit audit = new Audit(jsonObject.getString("changeMsg"), jsonObject.getLong("changedBy"), jsonObject.getLong("personId"), jsonObject.getString("timeStamp"));
                    auditList.add(audit);
                }
            }
            else{
                return auditList;
            }
        }catch(Exception e){
            LOGGER.error(e);
            throw new PersonException(e);
        }

        return auditList;
    }


    private String waitForResponseAsString(HttpRequestBase request) throws IOException {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;

        try {

            httpclient = HttpClients.createDefault();
            response = httpclient.execute(request);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    break;
                case 401:
                    throw new PersonException("401");
                default:
                    throw new PersonException("Non-200 status code returned: " + response.getStatusLine());
            }

            return parseResponseToString(response);

        } catch(Exception e) {
            throw new PersonException(e);
        } finally {
            if(response != null)
                response.close();
            if(httpclient != null)
                httpclient.close();
        }
    }

    public int insertPersonToDatabase(String firstName, String lastName, LocalDate dateOfBirth){
        HttpPost request = new HttpPost(wsURL);
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        request.setHeader("Authorization", sessionId);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");

        JSONObject credentials = new JSONObject();
        credentials.put("dateOfBirth", dateOfBirth);
        credentials.put("firstName", firstName);
        credentials.put("lastName", lastName);


        try {
            httpclient = HttpClients.createDefault();
            String credentialString = credentials.toString();
            StringEntity reqEntity = new StringEntity(credentialString);
            request.setEntity(reqEntity);
            response = httpclient.execute(request);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                        String stringResponse = parseResponseToString(response);
                        JSONObject jsonObject = new JSONObject(stringResponse);
                        return jsonObject.getInt("id");

                        case 401:
                        LOGGER.error("Invalid authorization");
                        throw new UnauthorizedException(response.getStatusLine().getReasonPhrase());
                case 400:
                    LOGGER.error("Invalid update request");
                    throw new UnauthorizedException(response.getStatusLine().getReasonPhrase());

                    default:
                        throw new UnknownException(response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException | UnknownException | NumberFormatException | UnauthorizedException e) {
            LOGGER.error(e.getMessage());
        }
        return 0;
    }

    public int updatePersonFromDatabase(Person oldPerson, Person newPerson){
        JSONObject credentials = new JSONObject();
        credentials.put("dateOfBirth", newPerson.getDateOfBirth());
        credentials.put("firstName", newPerson.getFirstName());
        credentials.put("lastName", newPerson.getLastName());

        if(!checkLock(newPerson))
            return -2;

        HttpPut request = new HttpPut(wsURL + "/" + newPerson.getId());
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        request.setHeader("Authorization", sessionId);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        int isUpdated = -1;
        try {
            httpclient = HttpClients.createDefault();
            String credentialString = credentials.toString();
            StringEntity reqEntity = new StringEntity(credentialString);
            request.setEntity(reqEntity);
            response = httpclient.execute(request);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    isUpdated = 0;
                    break;
                case 400:
                    LOGGER.error("Invalid update request");
                    throw new UnauthorizedException(response.getStatusLine().getReasonPhrase());
                case 401:
                    LOGGER.error("Invalid authorization");
                    throw new UnauthorizedException(response.getStatusLine().getReasonPhrase());
                default:
                    throw new UnknownException(response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException | UnknownException | NumberFormatException | UnauthorizedException e) {
            LOGGER.error(e.getMessage());
        }
        return isUpdated;
    }

    private boolean checkLock(Person updated){
        Person databasePerson = fetchPersonByID(updated.getId());
        System.out.println(updated.getCurrentTimeStamp());
        System.out.println(databasePerson.getCurrentTimeStamp());
        return updated.getCurrentTimeStamp().equals(databasePerson.getCurrentTimeStamp());
    }

    public Person fetchPersonByID(long id){
        HttpGet request = new HttpGet(wsURL + "/" +id);
        request.setHeader("Authorization", sessionId);
        Person person = new Person();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(response.getStatusLine().getStatusCode() == 200) {
            try {
                String responses = parseResponseToString(response);
                JSONObject root = new JSONObject(responses);
                LocalDate dateOfBirth;
                dateOfBirth = LocalDate.parse(root.getString("dateOfBirth"));
                ZonedDateTime currentTimeStamp;
                currentTimeStamp = ZonedDateTime.parse(root.getString("currentTimeStamp"));
                // Creates a person with the given information. Can cause parsing errors, however.
                person = new Person(root.getInt("id"),root.getString("firstName"), root.getString("lastName"),dateOfBirth, currentTimeStamp);
                person.setId(root.getInt("id"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return person;
    }


    public boolean deletePersonFromDatabase(long id){
        HttpDelete request = new HttpDelete(wsURL + "/" +id);
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        request.setHeader("Authorization", sessionId);
        boolean isDeleted = false;
        try {
            httpclient = HttpClients.createDefault();
            response = httpclient.execute(request);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    isDeleted = true;
                    break;
                case 401:
                    throw new UnauthorizedException(response.getStatusLine().getReasonPhrase());
                default:
                    throw new UnknownException(response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException | UnknownException | NumberFormatException | UnauthorizedException e) {
            LOGGER.error(e.getMessage());
        }
        return isDeleted;
    }

    public String getUserName(long id){
        HttpGet request = new HttpGet("http://localhost:8080/" + "users/" + id);
        request.setHeader("Authorization", sessionId);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String name = null;
        try {
            response = httpclient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(response.getStatusLine().getStatusCode() == 200) {
            try {
                name = parseResponseToString(response);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return name;
    }


    private String parseResponseToString(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        // use org.apache.http.util.EntityUtils to read json as string
        return EntityUtils.toString(entity, StandardCharsets.UTF_8);
    }


}
