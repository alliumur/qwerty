package pl.rmv.qwerty.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.rmv.qwerty.model.User;
import pl.rmv.qwerty.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {
    //@Autowired - zamiast można można zadeklarować pole i w konstruktorze zainicjalizować.
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    //---

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username);
    }
}
