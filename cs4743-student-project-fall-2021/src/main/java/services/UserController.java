package services;


import mvc.model.User;
import mvc.model.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pwhash.HashUtils;
import repository.SessionRepository;
import repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class UserController {
    private UserRepository userRepository;
    private static final Logger logger = LogManager.getLogger();

    public UserController(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<Session> logIn(@RequestBody Map<String, String> body) {
        List<User> listUser = userRepository.findAll();
        Session session = new Session();
        logger.info("[SPRING] Executing logIn");
        for(User user : listUser){
            if(user.getLogin().equals(body.get("username")) && user.getPassword().equals(body.get("password"))) {
                logger.info("[SPRING] Successfully found user id.");
                session.setUserId(user.getId());
                session.setSessionId(HashUtils.getCryptoHash(LocalTime.now().toString(),"SHA-256"));
                SessionController.createSession(session);
                return new ResponseEntity<>(session, HttpStatus.valueOf(200));
            }
        }
        logger.error("[SPRING] Could not find user id, or an error has occurred.");
        return new ResponseEntity<>(new Session(), HttpStatus.valueOf(401));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<String> getUserName(@RequestHeader Map<String, String> header, @PathVariable long userId){
        if (SessionController.validateSession(header.get("authorization"))) {
            Optional<User> user = userRepository.findById(userId);
            if(user.isPresent())
                return (new ResponseEntity<>(user.get().getLogin(), HttpStatus.valueOf(200)));
            else
                 return (new ResponseEntity<>(null, HttpStatus.valueOf(404)));
        }
        else return (new ResponseEntity<>(null, HttpStatus.valueOf(401)));
    }


}
