package repository;

import mvc.model.Person;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface SearchRepository extends CrudRepository<Person, Long> {
    @Query("SELECT p FROM Person p WHERE p.lastName LIKE ?1%")
    public ArrayList<Person> search(@Param("1") String keyword, Pageable pageable);

    @Query("SELECT COUNT(id) FROM Person p WHERE p.lastName LIKE ?1%")
    public long searchAmount(@Param("1") String keyword);
}
