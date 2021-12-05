package mvc.screens;

import exceptions.Alerts;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import mvc.model.Audit;
import mvc.model.Person;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.ResourceBundle;

/**
 * Deletes or Edits people based on their ID.
 * If their ID is zero, then that means they have just been created, thus they have to be added to the database
 * and / or the ListView arrayList.
 *  Else, the person has to be updated properly to the arrayList.
 */
public class PersonDetailController implements Initializable {

    private static final Logger LOGGER = LogManager.getLogger();

    private Person person;

    @FXML
    TextField name;

    @FXML
    TextField dob;

    @FXML
    Text error;

    @FXML
    Text idAndAge;

    @FXML
    ListView<String> auditListView;

    private ArrayList<Audit> audits;

    public PersonDetailController(Person person){
        this.person = person;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        error.setText("");
        name.setText(person.getFirstName() + " " + person.getLastName());
        dob.setText(person.getDateOfBirth().toString());
        idAndAge.setText("ID: " +person.getId() + " Age: " + person.getAge() + " Last Updated: " + person.getCurrentTimeStamp());
        updateChart();
    }

    @FXML
    void cancel(ActionEvent e){
        MainController.getInstance().switchView(ScreenType.LIST);
    }

    void updateChart(){
        audits = MainController.getInstance().getGateway().fetchAuditByPersonId(person.getId());
        ArrayList<String> show = new ArrayList<>();
        for(Audit a : audits){
            System.out.println(a.getChangedBy());
            show.add(a.getTimeStamp() + "\t" + MainController.getInstance().getGateway().getUserName(a.getChangedBy()) + "\t" + a.getChangeMsg());
        }
        ObservableList<String> tempList = FXCollections.observableList(show);
        auditListView.setItems(tempList);
    }

    /**
     * Once the Save button is pressed on the editor, this checks for the ID.
     * Depending on the persons ID, it will either create a person or edit the person
     * with the proper logging details.
     * @param e
     */
    @FXML
    void save(ActionEvent e){
        boolean newPerson = person.getId() == 0;
        if(checkFormat()) {
            if (newPerson)
                createPerson();
            else
                updatePerson();
            MainController.getInstance().switchView(ScreenType.LIST);

        }
        else {
            error.setText("Incorrect Formatting. Make sure to have first and last name, and correct DOB.");
            if(newPerson)
                LOGGER.error("Incorrect formatting when creating person");
            else
                LOGGER.error("Incorrect formatting when updating " +person.getFirstName());
        }

    }


    private void setPersonInfo(){
        String[] fullName;
        String[] date;
        try {
            fullName = name.getText().split(" ");
            date = dob.getText().split("-");

            person.setFirstName(fullName[0]);
            person.setLastName(fullName[1]);
            person.setDateOfBirth(LocalDate.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2])));
        }
        catch(IllegalFormatException | NumberFormatException | IndexOutOfBoundsException a){
            LOGGER.error(a.getMessage());
        }
    }

    /**
     * Insures correct input has been placed to avoid any weird exceptions or formatting errors
     * (Though they should be caught just in case).
     * Prevents blank names and DOB.
     * Ensures First Name and Last Name are between 1 - 100 (non inclusive).
     * Ensures the date can not be set to a date after the system time.
     * @return True if the formatting is correct, else returns false if there's an issue with formatting.
     */
    private boolean checkFormat(){
        String[] fullName;
        String[] date;
        LocalDate personDate = null;
        boolean correctFirstNameLength = false;
        boolean correctLastNameLength = false;

        fullName = name.getText().split(" ");
        date = dob.getText().split("-");

        if(fullName.length == 1 || fullName.length < 1)
            return false;
        if(fullName[0].length() > 1 && fullName[0].length() < 100)
            correctFirstNameLength = true;

        if(fullName[1].length() > 1 && fullName[1].length() < 100)
            correctLastNameLength = true;
        if(date.length > 2)
            personDate = LocalDate.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]));
        else
            return false;
        
        return correctLastNameLength && personDate.isBefore(ChronoLocalDate.from(LocalDateTime.now())) && correctFirstNameLength;
    }

    /**
     * Creates a person based on the inputted information.
     * TODO allow the database to increment the ID by one every time a person is added, or maybe use a random number?
     * Person's ID is automatically set to 1 after creation to allow for proper edits.
     */
    private void createPerson(){
        setPersonInfo();
        int id = MainController.getInstance().getGateway().insertPersonToDatabase(person.getFirstName(), person.getLastName(), person.getDateOfBirth());
        if(id == 0)
            LOGGER.error("Error has occurred on the Database side for creating person");
        else {
            person.setId(id);
            LOGGER.info("Creating " + person.getFirstName() + " " + person.getLastName() + " with id of " + id);
        }
    }

    /**
     * Updates the person's name or DOB.
     * TODO prevent the change of DOB (who would want to do that?), maybe more options like changing the ID(?)
     */
    private void updatePerson(){
        //setPersonInfo();
        String[] fullName = name.getText().split(" ");
        String[] date = dob.getText().split("-");

        try {
            Person newPerson = new Person(person.getId(), fullName[0], fullName[1], LocalDate.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2])), person.getCurrentTimeStamp());
            int result = MainController.getInstance().getGateway().updatePersonFromDatabase(person,newPerson);
            if(result == 0) {
                setPersonInfo();
                LOGGER.info("Updating " + person.getFirstName() + " " + person.getLastName());
                person = newPerson;
            }
            else if (result == -2){
                Alerts.infoAlert("Save error!", "This person has been modified by someone else!\nPlease redo your changes and try to save again.");
            }
            else
                LOGGER.error("Unable to update person.");
        }
        catch(IllegalFormatException | NumberFormatException | IndexOutOfBoundsException a){
            LOGGER.error(a.getMessage());
        }
    }


}
