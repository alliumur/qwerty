package pl.rmv.qwerty.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.rmv.qwerty.model.Role;
import pl.rmv.qwerty.model.User;
import pl.rmv.qwerty.service.UserService;

import java.util.*;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public String users(Model model) {
        model.addAttribute("users", userService.getUsers());
        return "users";
    }

    /**przekazanie do kontrolera parametr */
    //@GetMapping("{id}") [html -> <td><a th:href="${'/user/edit/'+user.id}">edytuj</a></td>]
    //public String edit(@PathVariable Long id, Model model){ return "user-edit"; }

    /**
     * spring na podstawie id oraz repozytorium może pobrać obiekt
     */
    @GetMapping("{user}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String edit(@PathVariable User user, Model model) {
        model.addAttribute("cuser", user);
        model.addAttribute("roles", Role.values());
        return "user-edit";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public String edit(
            @RequestParam("id") User user,
            @RequestParam("username") String username,
            @RequestParam Map<String, String> formparam) {

        userService.edit(user.getId(), username, null, null, null, new ArrayList<>(formparam.keySet()));
        return "redirect:/user";
    }

    @GetMapping("profile")
    public String profile(@AuthenticationPrincipal User user, Model model){
        model.addAttribute("id", user.getId());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        return "profile";
    }
    @PostMapping("profile")
    public String profile(@ModelAttribute("userForm") User user, @RequestParam("oldPassword") String oldPassword, Model model){
        if(userService.edit(user.getId(), user.getUsername(), user.getPassword(), oldPassword, user.getEmail(), null)){
            SecurityContextHolder.clearContext();
            return "redirect:/login";
        }else{
            model.addAttribute("username", user.getUsername());
            model.addAttribute("email", user.getEmail());
            return  "user/profile";
        }
    }
}
