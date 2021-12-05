package services;

import mvc.model.Person;
import mvc.model.Session;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import repository.SessionRepository;

import java.util.List;
import java.util.Map;

@RestController
public class SessionController {

    private static SessionRepository sessionRepository;

    public SessionController(SessionRepository sessionRepository){
        SessionController.sessionRepository = sessionRepository;
    }

    public static void createSession(Session session){
        List<Session> sessionList;
        sessionList = sessionRepository.findAll();
        for(Session sesh : sessionList){
            if(sesh.getUserId() == session.getUserId())
                sessionRepository.delete(sesh);
        }
        sessionRepository.save(session);
    }

    public static boolean validateSession(String sessionId){
        List<Session> sessionList;
        sessionList = sessionRepository.findAll();
        for(Session sesh : sessionList){
            if(sesh.getSessionId().equals(sessionId))
                return true;
        }
        return false;
    }

    public static ResponseEntity<Session> getSession(String sessionId){
        if(sessionId.length() > 0){
            List<Session> sessionList = sessionRepository.findAll();
            for(Session s : sessionList)
                if(s.getSessionId().equals(sessionId))
                    return new ResponseEntity<>(s, HttpStatus.valueOf(200));
        }
        else
            return new ResponseEntity<>(new Session(), HttpStatus.valueOf(400));
        return new ResponseEntity<>(new Session(), HttpStatus.valueOf(404));
    }
}
