package services;

import mvc.model.Person;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.List;

public interface SearchServiceInterface {
    public ArrayList<Person> search(String keyword);
    public long searchAmount(String keyword);
}
