package services;

import mvc.model.Audit;
import mvc.model.Person;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repository.AuditRepository;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.Collections. *;

@RestController
public class AuditController {

    private static AuditRepository auditRepository;
    private static final Logger logger = LogManager.getLogger();

    public AuditController(AuditRepository auditRepository){
        this.auditRepository = auditRepository;
    }

    @GetMapping("/people/{personId}/audittrail")
    public ResponseEntity<List<Audit>> fetchAuditById(@PathVariable long personId, @RequestHeader Map<String, String> header){
        List<Audit> audit= auditRepository.findAll();
        ArrayList<Audit> resultAuditList = new ArrayList<>();
        logger.info("[SPRING] Executing fetchAuditById");
        if(SessionController.validateSession(header.get("authorization"))) {
            if (audit.size() > 0) {
                logger.info("[SPRING] Successfully found audit list.");
                for(Audit a : audit){
                    if (a.getPersonId() == personId)
                        resultAuditList.add(a);
                }
                if(resultAuditList.size() > 0)
                    return new ResponseEntity<>(resultAuditList, HttpStatus.valueOf(200));
                else
                    return new ResponseEntity<>(resultAuditList, HttpStatus.valueOf(404));
            }
        }
        else
            return new ResponseEntity<>(resultAuditList, HttpStatus.valueOf(401));
        logger.error("[SPRING] Unable to find person by id, or an error has occurred.");
        return new ResponseEntity<>(resultAuditList, HttpStatus.valueOf(404));
    }

    @PostMapping("/people/audittrail")
    public ResponseEntity<Audit> insertAudit(@RequestBody Map<String, String> body, @RequestHeader Map<String, String> header){
        if(SessionController.validateSession(header.get("authorization"))){
            if(body.get("changeMsg").length() > 0 && body.get("changedBy").length() < 100 && body.get("personId").length() < 100){
                Audit audit = auditRepository.save(new Audit(body.get("changeMsg"), Long.parseLong(body.get("changedBy")), Long.parseLong(body.get("personId"))));
                return new ResponseEntity<>(audit, HttpStatus.valueOf(200));
            }
            else
                return new ResponseEntity<>(new Audit(), HttpStatus.valueOf(400));
        }
        else
            return new ResponseEntity<>(new Audit(), HttpStatus.valueOf(401));
    }

    protected static void updateAudit(String message, String sessionId, long personId){
        long userId = Objects.requireNonNull(SessionController.getSession(sessionId).getBody()).getUserId();
        CloseableHttpClient httpclient = null;
        HttpPost request = new HttpPost("http://localhost:8080/people/audittrail");
        request.setHeader("authorization", sessionId);
        request.setHeader("Authorization", sessionId);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        JSONObject credentials = new JSONObject();
        credentials.put("changeMsg", message);
        credentials.put("changedBy", userId);
        credentials.put("personId", personId);
        try {
            request.setEntity(new StringEntity(credentials.toString()));
            httpclient = HttpClients.createDefault();
            httpclient.execute(request);
            httpclient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



}
