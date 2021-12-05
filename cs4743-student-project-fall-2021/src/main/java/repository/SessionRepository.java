package repository;

import org.springframework.data.jpa.repository.JpaRepository;
import mvc.model.Session;
public interface SessionRepository  extends JpaRepository<Session, Long> {
}
