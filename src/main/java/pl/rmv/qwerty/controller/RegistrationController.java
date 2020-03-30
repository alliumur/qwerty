package pl.rmv.qwerty.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import pl.rmv.qwerty.model.Role;
import pl.rmv.qwerty.model.User;
import pl.rmv.qwerty.repository.UserRepository;
import pl.rmv.qwerty.service.UserService;

import java.util.Collections;

@Controller
public class RegistrationController {
    @Autowired
    private UserService userService;

    @GetMapping("/registration")
    private String registration(){
        return "registration";
    }

    @PostMapping("/registration")
    private RedirectView registration(@ModelAttribute("userForm") User user, RedirectAttributes model){
        if(!userService.add(user)){
            model.addFlashAttribute("message", user.getUsername() + " już istnieje");
            model.addFlashAttribute("username", user.getUsername());
            model.addFlashAttribute("email", user.getEmail());
            return new RedirectView("registration", true);
        }
        model.addFlashAttribute("warning","Na podany email został wysłany list z linkiem aktywacyjnym");
        return new RedirectView("/login",true);
    }

    @GetMapping("/activate/{code}")
    public String activate(Model model, @PathVariable String code){
        boolean isActivate = userService.activate(code);
        if(isActivate){
            model.addAttribute("message", "Aktywacja przebiegła pomyślnie");
        }else {
            model.addAttribute("error", "Niewłaściwy kod aktywacyjny");
        }
        return "login";
    }
}
