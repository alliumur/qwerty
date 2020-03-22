package pl.rmv.qwerty.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.rmv.qwerty.model.Message;
import pl.rmv.qwerty.model.User;
import pl.rmv.qwerty.repository.MessageRepository;

@Controller
public class MainController {
    @Autowired
    MessageRepository messageRepository;
    @GetMapping("/")
    public String welcome(
            @RequestParam(name = "name", required = false, defaultValue = "Qwerty") String name, Model model){
        model.addAttribute("name", name);
        return "welcome";
    }

    @GetMapping("/index")
    public String index(Model model){
        Iterable<Message> messages = messageRepository.findAll();
        model.addAttribute("messages", messages);
        return "index";
    }

    @PostMapping("/createmessage")
    public String createmessage(
            @AuthenticationPrincipal User user,
            @RequestParam String text,
            @RequestParam String tag,
            Model model){
        if(!text.isEmpty() && !tag.isEmpty()){
            messageRepository.save(new Message(text, tag, user));
        }
        Iterable<Message> messages = messageRepository.findAll();
        model.addAttribute("messages", messages);
        return "index";
    }

    @PostMapping("/filtermessage")
    public String filtermessage(@RequestParam String tag, Model model){
        Iterable<Message> messages;
        if(tag.isEmpty()){
            messages = messageRepository.findAll();
        }else{
            messages = messageRepository.findByTag(tag);
        }
        model.addAttribute("messages", messages);
        return "index";
    }
}
