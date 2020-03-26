package pl.rmv.qwerty.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import pl.rmv.qwerty.model.Message;
import pl.rmv.qwerty.model.User;
import pl.rmv.qwerty.repository.MessageRepository;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
public class MainController {
    @Autowired
    private MessageRepository messageRepository;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/")
    public String welcome(
            @RequestParam(name = "name", required = false, defaultValue = "Qwerty") String name, Model model){
        model.addAttribute("name", name);
        return "welcome";
    }

    @GetMapping("/home")
    public String index(@RequestParam(required = false) String filter, Model model){
        Iterable<Message> messages;
        if(filter != null && !filter.isEmpty()){
            messages = messageRepository.findByTag(filter);
        }else{
            messages = messageRepository.findAll();
        }
        model.addAttribute("messages", messages);
        model.addAttribute("filter", filter);

        return "home";
    }

    @PostMapping("/createmessage")
    public String createmessage(
            @AuthenticationPrincipal User user,
            @RequestParam String text,
            @RequestParam String tag,
            @RequestParam("file") MultipartFile file,
            Model model) throws IOException {

        //obsługa zasobów wysłanych na serwer
        String fullFileName = "";
        boolean isEmptyFilename = file.getOriginalFilename().isEmpty();
        if(file != null && !isEmptyFilename){
            File uploadDir = new File(uploadPath);
            if(!uploadDir.exists()){
                uploadDir.mkdir();
            }
            String uuid = UUID.randomUUID().toString();
            fullFileName = uuid + "." + file.getOriginalFilename();
            file.transferTo(new File(uploadDir + "/" + fullFileName));
        }

        if(!text.isEmpty() && !tag.isEmpty()){
            messageRepository.save(new Message(text, tag, user, isEmptyFilename ? null : fullFileName));
        }
        Iterable<Message> messages = messageRepository.findAll();
        model.addAttribute("messages", messages);
        return "home";
    }
}
