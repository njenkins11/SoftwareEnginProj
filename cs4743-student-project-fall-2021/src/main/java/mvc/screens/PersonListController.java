package mvc.screens;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import mvc.model.FetchResults;
import mvc.model.Person;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.jandex.Main;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PersonListController implements Initializable {

    private static final Logger LOGGER = LogManager.getLogger();

    private ArrayList<Person> persons;
    private FetchResults fetchResults;

    @FXML
    private ListView<Person> personListView;

    @FXML
    private TextField searchField;

    @FXML
    private Label searchInfo;

    @FXML
    private Button first;

    @FXML
    private Button last;

    @FXML
    private Button prev;

    @FXML
    private Button next;

    public PersonListController(ArrayList<Person> persons, FetchResults fetchResults) {
        this.persons = persons;
        this.fetchResults = fetchResults;
    }

    private void updateButtons(){
        prev.setDisable(fetchResults.getCurrentPage() == 0);
        next.setDisable(fetchResults.getCurrentPage() == fetchResults.getMaxPage()-1);
        if(persons.size() == 0){
            prev.setDisable(true);
            next.setDisable(true);
            first.setDisable(true);
            last.setDisable(true);
        }
    }

    private void updateScene(){
        updateButtons();
        ObservableList<Person> tempList = FXCollections.observableList(persons);
        personListView.setItems(tempList);
        searchInfo.setText("Fetched records " + (((fetchResults.getPageSize() * (fetchResults.getCurrentPage()+1)) - fetchResults.getPageSize())+1) + " to " +(fetchResults.getPageSize() * (fetchResults.getCurrentPage()+1))+ " out of " +fetchResults.getMaxElements() +" records.");
    }

    @FXML
    void nextPage(){
        fetchResults = MainController.getInstance().getGateway().fetchPersons(fetchResults.getCurrentPage()+1,"" );
        persons = fetchResults.getPersons();

        updateScene();
    }

    @FXML
    void prevPage(){
        fetchResults = MainController.getInstance().getGateway().fetchPersons(fetchResults.getCurrentPage()-1,"" );
        persons = fetchResults.getPersons();
        updateScene();
    }

    @FXML
    void firstPage(){
        fetchResults = MainController.getInstance().getGateway().fetchPersons(0,"" );
        persons = fetchResults.getPersons();
        updateScene();
    }

    @FXML
    void lastPage(){
        fetchResults = MainController.getInstance().getGateway().fetchPersons(fetchResults.getMaxPage()-1,"" );
        persons = fetchResults.getPersons();
        updateScene();
    }

    @FXML
    void search(){
        String search = searchField.getText();
        fetchResults = MainController.getInstance().getGateway().fetchPersons(0, search);
        persons = fetchResults.getPersons();
        updateScene();
    }

    /**
     * Edits a person within the list view once the user double-clicks on a person.
     * Calls on the same Edit Screen as "addPerson".
     * TODO have this update to a database with proper checks in place.
     * @param event
     */
    @FXML
    void editPerson(MouseEvent event){

        if(event.getClickCount() == 2) {
            Person clickedPerson = personListView.getSelectionModel().getSelectedItems().get(0);
            LOGGER.info("READING " + clickedPerson.getFirstName() + " " + clickedPerson.getLastName());
            MainController.getInstance().switchView(ScreenType.DETAIL, clickedPerson);
        }
    }

    /**
     * Deletes selected person(s) from ListView. Can cause IndexOutOfBounds if there isn't a person selected.
     * TODO allow for multiple removals at once and update to a database.
     * @param e
     */
    @FXML
    void deletePerson(ActionEvent e){
        try {
            Person clickedPerson = personListView.getSelectionModel().getSelectedItems().get(0);
            if(MainController.getInstance().getGateway().deletePersonFromDatabase(clickedPerson.getId())) {
                personListView.getItems().remove(clickedPerson);
                LOGGER.info("DELETING " + clickedPerson.getFirstName() + " " + clickedPerson.getLastName());
            }
        }catch(IndexOutOfBoundsException ex){
            LOGGER.error("No person selected for deleting." + ex.getMessage());
        }
        firstPage();

    }

    /**
     * Adds a person to the ListView by calling on the Detail Controller.
     * Uses the same controller as editPerson.
     * @param e
     */
    @FXML
    void addPerson(ActionEvent e){
        MainController.getInstance().switchView(ScreenType.DETAIL, new Person(0, "", "", LocalDate.of(0,1,1)));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Allows for updating the ListView since array list is observing if any data has been changed
        ObservableList<Person> tempList = FXCollections.observableList(persons);
        personListView.setItems(tempList);
        searchInfo.setText("Fetched records " + (fetchResults.getCurrentPage() + 1 ) + " to " +fetchResults.getPageSize()+ " out of " +fetchResults.getMaxElements() +" records.");
        updateButtons();
    }
}
