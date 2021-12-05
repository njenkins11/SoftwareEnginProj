package mvc.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZonedDateTime;

@Entity
@Table(name = "People")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;
    @Column(name = "age", nullable = false)
    private int age;
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    @Column(name = "dob", nullable = false)
    private LocalDate dateOfBirth;
    @Column(name = "update_timestamp")
    private ZonedDateTime currentTimeStamp;

    public Person(long id, String firstName, String lastName, LocalDate dateOfBirth, ZonedDateTime currentTimeStamp){
        this.id         = id;
        this.firstName  = firstName;
        this.lastName   = lastName;
        this.dateOfBirth = dateOfBirth;
        age = calculateAge();
        this.currentTimeStamp = currentTimeStamp;
    }

    public Person(long id, String firstName, String lastName, LocalDate dateOfBirth) {
        this.id         = id;
        this.firstName  = firstName;
        this.lastName   = lastName;
        this.dateOfBirth = dateOfBirth;
        age = calculateAge();
    }

    public Person(String firstName, String lastName, LocalDate dateOfBirth){
        this.firstName  = firstName;
        this.lastName   = lastName;
        this.dateOfBirth = dateOfBirth;
        age = calculateAge();
    }

    public Person(){
        id  = 0;
        firstName   = "";
        lastName    = "";
        setDateOfBirth(LocalDate.now());
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + " " + dateOfBirth + " " + age;
    }

    private int calculateAge(){
        return Year.now().getValue() - dateOfBirth.getYear();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        age = calculateAge();
    }

    public ZonedDateTime getCurrentTimeStamp() {
        return currentTimeStamp;
    }

}
