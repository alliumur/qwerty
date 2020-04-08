package pl.rmv.qwerty.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import pl.rmv.qwerty.model.Role;
import pl.rmv.qwerty.model.User;
import pl.rmv.qwerty.repository.UserRepository;
import pl.rmv.qwerty.service.UserService;
import pl.rmv.qwerty.utils.ControllerUtils;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

@Controller
public class RegistrationController {
    @Autowired
    private UserService userService;

    @GetMapping("/registration")
    private String registration(){
        return "registration";
    }

    @PostMapping("/registration")
    private String registration(@Valid User user, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes){
        if(userService.exist(user.getUsername())){
            bindingResult.rejectValue("username", "error.user", "Użytkownik istnieje z taką nazwą");
        }else if(user.getPassword() != null && !user.getPassword().equals(user.getPasswordConfirm())){
            bindingResult.rejectValue("passwordConfirm", "error.user", "Hasło różni się od poprzedniego");
        }

        if(bindingResult.hasErrors()){
            Map<String, String> errorsMap = ControllerUtils.getErrors(bindingResult);
            model.mergeAttributes(errorsMap);
            model.addAttribute("user", user);
            return "registration";
        }

        userService.add(user);
        redirectAttributes.addFlashAttribute("warning","Na podany email został wysłany list z linkiem aktywacyjnym");
        model.addAttribute("user", user);
        return "redirect:/login";
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
