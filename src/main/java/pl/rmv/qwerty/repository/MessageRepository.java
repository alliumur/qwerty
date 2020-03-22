package pl.rmv.qwerty.repository;

import org.springframework.data.repository.CrudRepository;
import pl.rmv.qwerty.model.Message;

public interface MessageRepository extends CrudRepository<Message, Long> {
    Iterable<Message> findByTag(String tag);
}
