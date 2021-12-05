package mvc.screens;

import exceptions.UnauthorizedException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import mvc.model.Audit;
import mvc.model.FetchResults;
import mvc.model.Person;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pwhash.HashUtils;
import client.PersonGatewayAPI;
import mvc.model.Session;
import client.SessionGateway;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Deals with controlling the screens of this application.
 * Singleton of this assignment.
 */
public class MainController implements Initializable {

    private static final Logger LOGGER = LogManager.getLogger();

    private Session session;
    private PersonGatewayAPI gateway;

    @FXML
    private BorderPane rootPane;
    @FXML
    private TextField userName;
    @FXML
    private TextField password;
    @FXML
    private Text error;

    private static MainController instance = null;

    private ArrayList<Person> persons;
    private FetchResults fetchResults;

    private String user;

    private MainController(){
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        error.setText("");
    }

    /**
     * Logs users based on their username and password (duh).
     *
     */
    @FXML
    private void logIn(){

        if(!userName.getText().isBlank() && !password.getText().isBlank()) {
            try {
                session = SessionGateway.authenticate(userName.getText(), HashUtils.getCryptoHash(password.getText(), "SHA-256"));
                user = userName.getText();
                gateway = new PersonGatewayAPI(session.getUrl() + "/people", session.getSessionId());
                loadPersonArrayList();
                switchView(ScreenType.LIST);
            } catch (UnauthorizedException e) {
                LOGGER.error(e.getMessage());
            }
        }
        else
            error.setText("Please Enter a Username and a Password");

    }

    public void switchView(ScreenType screenType, Object ... args){
        FXMLLoader loader;
        Parent rootNode;
        switch(screenType){
            case LIST:
                loadPersonArrayList();
                loader = new FXMLLoader(this.getClass().getResource("/PersonList.fxml"));
                loader.setController(new PersonListController(persons, fetchResults));
                try {
                    rootNode = loader.load();
                    rootPane.setCenter(rootNode);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
                break;
            case DETAIL:
                loader = new FXMLLoader(this.getClass().getResource("/PersonEditor.fxml"));
                if(!(args[0] instanceof Person)) {
                    throw new IllegalArgumentException("Hey that's not a person! " + args[0].toString());
                }
                loader.setController(new PersonDetailController((Person) args[0]));

                try {
                    rootNode = loader.load();
                    rootPane.setCenter(rootNode);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
                break;
            case LOGIN:
                loader = new FXMLLoader(this.getClass().getResource("/Login.fxml"));
                loader.setController(MainController.getInstance());
                try {
                    rootNode = loader.load();
                    rootPane.setCenter(rootNode);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
                break;
        }

    }

    public void loadPersonArrayList(){
        fetchResults = gateway.fetchPersons(0, "");
        persons = fetchResults.getPersons();
    }

    // Getters Setters
    public ArrayList<Person> getPersons() {
        return persons;
    }

    public void setPersons(ArrayList<Person> persons) {
        this.persons = persons;
    }

    public static MainController getInstance(){
        if(instance == null)
            instance = new MainController();
        return instance;
    }

    public Session getSession(){
        return session;
    }

    public PersonGatewayAPI getGateway() {
        return gateway;
    }

    public String getUser(){return user;}
}
