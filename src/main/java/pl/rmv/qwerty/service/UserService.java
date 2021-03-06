package pl.rmv.qwerty.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private MailService mailsender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${hostname}")
    private String hostname;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if(user == null){
            throw new UsernameNotFoundException("");
        }
        return user;
    }

    public boolean exist(String username){
        return userRepository.findByUsername(username) != null;
    }

    public void add(User user){
        if(user.getUsername().equals("admin")){
            user.setActive(true);
            user.setRoles(Collections.singleton(Role.ADMIN));
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }else {
            user.setActive(false);
            user.setRoles(Collections.singleton(Role.USER));
            user.setActivationCode(UUID.randomUUID().toString());
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            if(!StringUtils.isEmpty(user.getEmail())){
                String message = String.format(
                        "Witaj %s!, \n" +
                                "Miło nam że jesteś z nami. Żeby dokończyć rejestrację, przejdź: http://%s/activate/%s",
                        user.getUsername(),
                        hostname,
                        user.getActivationCode()
                );

                mailsender.send(user.getEmail(), "Activation code", message);
            }
        }
        userRepository.save(user);
    }

    public boolean activate(String code) {
        User user = userRepository.findByActivationCode(code);
        if(user == null){
            return false;
        }
        user.setActive(true);
        user.setActivationCode(null);
        user.setPasswordConfirm(user.getPassword());
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

    public User getUser(String username){
        return  userRepository.findByUsername(username);
    }

    public void subscribe(User currentUser, User user) {
        user.getSubscribers().add(currentUser);
        userRepository.save(user);
    }

    public void unsubscribe(User currentUser, User user) {
        user.getSubscribers().remove(currentUser);
        userRepository.save(user);
    }
}
