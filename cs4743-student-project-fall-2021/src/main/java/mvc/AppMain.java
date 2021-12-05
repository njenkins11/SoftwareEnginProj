package mvc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mvc.model.Person;
import mvc.screens.MainController;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Author: Nicole Jenkins
 * abc123: wfh837
 * Assignment 3
 */

@SpringBootApplication
@ComponentScan({"services"})
@EntityScan("mvc.model")
@EnableJpaRepositories({"repository"})
public class AppMain extends Application {

    private ArrayList<Person> persons;

    public static void main(String[] args){
        SpringApplication.run(AppMain.class, args);
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/Login.fxml"));
        loader.setController(MainController.getInstance());
        MainController.getInstance().setPersons(persons);
        Parent rootNode = loader.load();
        stage.setScene(new Scene(rootNode));
        stage.setTitle("Assignment 4");
        stage.show();

    }

    public void init() throws Exception{
        super.init();
        persons = new ArrayList<>();
        persons.add(new Person(1, "Edward","Hyde", LocalDate.of(1980,2,25)));
        persons.add(new Person(1, "Henry","Jekyll", LocalDate.of(1970,5,2)));
        persons.add(new Person(1, "Billy","Bob", LocalDate.of(1956,7,20)));
        persons.add(new Person(1, "Joe","Blue", LocalDate.of(1999,1,10)));
    }
    public void stop() throws Exception{
        super.stop();
    }


}
