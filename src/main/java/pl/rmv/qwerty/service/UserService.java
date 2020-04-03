package pl.rmv.qwerty.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import pl.rmv.qwerty.model.Role;
import pl.rmv.qwerty.model.User;
import pl.rmv.qwerty.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    //@Autowired - zamiast można można zadeklarować pole i w konstruktorze zainicjalizować.
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Autowired
    private MailSender mailsender;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username);
    }

    public boolean add(User user){
        if(userRepository.findByUsername(user.getUsername()) != null){
            return false;
        }
        user.setActive(false);
        user.setRoles(Collections.singleton(Role.USER));
        user.setActivationCode(UUID.randomUUID().toString());
        userRepository.save(user);

        if(!StringUtils.isEmpty(user.getEmail())){
            String message = String.format(
                    "Witaj %s!, \n" +
                    "Miło nam że jesteś z nami. Żeby dokończyć rejestrację, przejdź: http://localhost:8080/activate/%s",
                    user.getUsername(),
                    user.getActivationCode()
            );

            mailsender.send(user.getEmail(), "Activation code", message);
        }
        return true;
    }

    public boolean activate(String code) {
        User user = userRepository.findByActivationCode(code);
        if(user == null){
            return false;
        }
        user.setActive(true);
        user.setActivationCode(null);
        userRepository.save(user);
        return true;
    }
    
    public boolean edit(Long id, String username, String password, String oldPassword, String email, List<String> userRoles){
        User user = userRepository.findById(id).get();
        if(user == null) return false;
        boolean edit = false;

        if(!StringUtils.isEmpty(username) && !user.getUsername().equals(username)){
            user.setUsername(username);
            edit = true;
        }

        if(!StringUtils.isEmpty(password)
                && !StringUtils.isEmpty(oldPassword)
                && user.getPassword().equals(oldPassword)
                && !password.equals(oldPassword)){
            user.setPassword(password);
            edit = true;
        }

        if(!StringUtils.isEmpty(email) && !user.getEmail().equals(email)){
            user.setActive(false);
            user.setActivationCode(UUID.randomUUID().toString());
            String message = String.format(
                    "Zmiania danych w serwisie qwerty. \n" +
                            "Email zostal zmieniony na aktualny. Żeby ponownie aktywować aplikację przejdź http://localhost:8080/activate/%s",
                    user.getUsername(),
                    user.getActivationCode()
            );
            mailsender.send(user.getEmail(), "Activation code", message);
            edit = true;
        }

        if(userRoles != null){
            Set<String> roles = Arrays.stream(Role.values())
                    .map(Role::name)
                    .collect(Collectors.toSet());
            user.getRoles().clear();
            for (String param : userRoles) {
                if(roles.contains(param)){
                    user.getRoles().add(Role.valueOf(param));
                }
            }
            edit = true;
        }
        if(edit) userRepository.save(user);
        return true;
    }

    public Iterable<User> getUsers(){
        return userRepository.findAll();
    }

    public User getUser(Long id){ return userRepository.findById(id).get(); }
}
