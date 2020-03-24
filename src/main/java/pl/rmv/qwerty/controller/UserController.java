package pl.rmv.qwerty.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.rmv.qwerty.model.Role;
import pl.rmv.qwerty.model.User;
import pl.rmv.qwerty.repository.UserRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
@PreAuthorize("hasAuthority('ADMIN')")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }

    /**przekazanie do kontrolera parametr */
    //@GetMapping("{id}") [html -> <td><a th:href="${'/user/edit/'+user.id}">edytuj</a></td>]
    //public String edit(@PathVariable Long id, Model model){ return "user-edit"; }

    /**
     * spring na podstawie id oraz repozytorium może pobrać obiekt
     */
    @GetMapping("{user}")
    public String edit(@PathVariable User user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "user-edit";
    }

    @PostMapping
    public String edit(
            @RequestParam("id") User user,
            @RequestParam("username") String username,
            @RequestParam Map<String, String> formparam) {

        user.setUsername(username);

        Set<String> roles = Arrays.stream(Role.values())
                .map(Role::name)
                .collect(Collectors.toSet());
        user.getRoles().clear();
        for (String param : formparam.keySet()) {
            if(roles.contains(param)){
                user.getRoles().add(Role.valueOf(param));
            }
        }

        userRepository.save(user);
        return "redirect:/user";
    }
}
