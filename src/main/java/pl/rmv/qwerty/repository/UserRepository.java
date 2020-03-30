package pl.rmv.qwerty.repository;

import org.springframework.data.repository.CrudRepository;
import pl.rmv.qwerty.model.User;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);

    User findByActivationCode(String code);
}
