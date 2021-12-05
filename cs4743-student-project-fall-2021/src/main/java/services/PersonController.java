package services;

import mvc.model.FetchResults;
import mvc.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import repository.PersonRepository;
import repository.SearchRepository;

import javax.persistence.Temporal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
public class PersonController {
    private static final Logger logger = LogManager.getLogger();

    private PersonRepository personRepository;

    @Autowired
    private SearchRepository searchRepository;

    public PersonController(PersonRepository personRepository){
        this.personRepository = personRepository;
    }

    /**
    @GetMapping("/people")
    public ResponseEntity<List<Person>> fetchPeople(@RequestHeader Map<String, String> header){
        if(SessionController.validateSession(header.get("authorization"))){
            List<Person> personList = personRepository.findAll();
            logger.info("[SPRING] Executing fetchPeople");
            return new ResponseEntity<>(personList, HttpStatus.valueOf(200));
        }
        else
            return new ResponseEntity<>(null, HttpStatus.valueOf(401));
    }
     **/

    @GetMapping("/people")
    public ResponseEntity<FetchResults> fetchPaginatedList(@RequestHeader Map<String, String> header, @PageableDefault(size = 10) Pageable pageable, @RequestParam(required = false) String keyword){
        FetchResults results;
        if(SessionController.validateSession(header.get("authorization")))
        {
            if(keyword != null)
            {
                return new ResponseEntity<>(new FetchResults(pageable.getPageSize(), pageable.getPageNumber(), searchRepository.searchAmount(keyword),searchRepository.search(keyword, pageable)), HttpStatus.valueOf(200));
            }
            return new ResponseEntity<>(new FetchResults(pageable.getPageSize(), pageable.getPageNumber(), searchRepository.searchAmount(""),searchRepository.search("", pageable)), HttpStatus.valueOf(200));
        }
        else
            return new ResponseEntity<>(null, HttpStatus.valueOf(401));

    }

    @GetMapping("/people/{personId}")
    public ResponseEntity<Person> fetchPersonById(@PathVariable long personId, @RequestHeader Map<String, String> header){
        Optional<Person> person = personRepository.findById(personId);
        logger.info("[SPRING] Executing fetchPersonById");
        if(SessionController.validateSession(header.get("authorization"))) {
            if (person.isPresent()) {
                logger.info("[SPRING] Successfully found person by id.");
                return new ResponseEntity<>(person.get(), HttpStatus.valueOf(200));
            }
        }
        else
            return new ResponseEntity<>(new Person(), HttpStatus.valueOf(401));
        logger.error("[SPRING] Unable to find person by id, or an error has occurred.");
        return new ResponseEntity<>(new Person(), HttpStatus.valueOf(404));
    }

    @PostMapping("/people")
    public ResponseEntity<Person> insertPerson(@RequestBody Map<String, String> body, @RequestHeader Map<String, String> header){
        if(SessionController.validateSession(header.get("authorization"))){
            if((body.get("firstName").length() > 0 && body.get("firstName").length() < 100) &&
                    (body.get("lastName").length() > 0 && body.get("lastName").length() < 100)){
                if(LocalDate.now().isAfter(LocalDate.parse(body.get("dateOfBirth")))){
                    Person person = personRepository.save(new Person(body.get("firstName"),body.get("lastName"),LocalDate.parse(body.get("dateOfBirth"))));
                    AuditController.updateAudit("added", header.get("authorization"), person.getId());
                    return new ResponseEntity<>(person, HttpStatus.valueOf(200));
                }
                else
                    return new ResponseEntity<>(new Person(), HttpStatus.valueOf(400));
            }
            else
                return new ResponseEntity<>(new Person(), HttpStatus.valueOf(400));
        }
        else
            return new ResponseEntity<>(new Person(), HttpStatus.valueOf(401));
    }

    @DeleteMapping("/people/{personId}")
    public ResponseEntity<Person> deletePerson(@PathVariable long personId, @RequestHeader Map<String, String> header){
        if(SessionController.validateSession(header.get("authorization"))){
            try {
                if(personRepository.existsById(personId)) {
                    personRepository.deleteById(personId);
                    //AuditController.updateAudit("removed", header.get("authorization"), personId);
                    return new ResponseEntity<>(new Person(), HttpStatus.valueOf(200));
                }
                else
                    return new ResponseEntity<>(new Person(), HttpStatus.valueOf(404));
            }catch(NumberFormatException e){
                logger.error(e);
            }
        }
        return new ResponseEntity<>(new Person(), HttpStatus.valueOf(401));
    }

    @PutMapping("people/{personId}") public ResponseEntity<Person> insertPerson(@PathVariable long personId, @RequestBody Map<String, String> body, @RequestHeader Map<String, String> header) {
        if (SessionController.validateSession(header.get("authorization"))) {
            if ((body.get("firstName").length() > 0 && body.get("firstName").length() < 100) &&
                    (body.get("lastName").length() > 0 && body.get("lastName").length() < 100)) {
                if (LocalDate.now().isAfter(LocalDate.parse(body.get("dateOfBirth")))) {
                    if(personRepository.existsById(personId)){
                        Optional<Person> oldPersonOpt = personRepository.findById(personId);
                        Person oldPerson = new Person(oldPersonOpt.get().getFirstName(),oldPersonOpt.get().getLastName(),oldPersonOpt.get().getDateOfBirth());
                        Person person = personRepository.save(new Person(personId, body.get("firstName"), body.get("lastName"), LocalDate.parse(body.get("dateOfBirth"))));
                        updatePersonAudit(oldPerson, person, header.get("authorization"));
                        return new ResponseEntity<>(person, HttpStatus.valueOf(200));
                    }
                    else
                        return new ResponseEntity<>(new Person(), HttpStatus.valueOf(404));
                } else
                    return new ResponseEntity<>(new Person(), HttpStatus.valueOf(400));
            } else
                return new ResponseEntity<>(new Person(), HttpStatus.valueOf(400));
        } else
            return new ResponseEntity<>(new Person(), HttpStatus.valueOf(401));
    }

    private void updatePersonAudit(Person oldPerson, Person newPerson, String session){
        String response = "";
        if(!(oldPerson.getLastName().equals(newPerson.getLastName())  && oldPerson.getFirstName().equals(newPerson.getFirstName()))){
            response += oldPerson.getFirstName() + " " +oldPerson.getLastName() + " changed to " + newPerson.getFirstName() + " " + newPerson.getLastName() + " ";
        }

        if(!(oldPerson.getDateOfBirth().equals(newPerson.getDateOfBirth()))){
            response += oldPerson.getDateOfBirth() + " changed to " + newPerson.getDateOfBirth();
        }

        if(!response.equals("")) {
            AuditController.updateAudit(response, session, newPerson.getId());
        }

    }


}
