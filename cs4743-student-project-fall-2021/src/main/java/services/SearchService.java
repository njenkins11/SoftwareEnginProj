package services;

import mvc.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import repository.SearchRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService implements SearchServiceInterface{
    @Autowired
    private SearchRepository searchService;
    @Override
    public ArrayList<Person> search(String keyword) {
        return (ArrayList<Person>) searchService.search(keyword, PageRequest.of(1, 20));
    }

    @Override
    public long searchAmount(String keyword){
        return searchService.searchAmount(keyword);
    }
}
