package pl.rmv.qwerty.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import pl.rmv.qwerty.model.Message;
import pl.rmv.qwerty.model.Role;
import pl.rmv.qwerty.model.User;
import pl.rmv.qwerty.repository.MessageRepository;
import pl.rmv.qwerty.service.UploadService;
import pl.rmv.qwerty.utils.ControllerUtils;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Controller
public class MainController {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UploadService uploadService;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/")
    public String welcome(
            @RequestParam(name = "name", required = false, defaultValue = "Qwerty") String name, Model model){
        model.addAttribute("name", name);
        return "welcome";
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal User user, HttpSession session, @RequestParam(required = false) String filter, Model model){
        session.setAttribute("isLogged", true);
        session.setAttribute("isAdmin", user.getRoles().contains(Role.ADMIN));

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
            @Valid Message message,
            BindingResult bindingResult, // zawsze musi być przed 'model', żeby błędy nie były przechwytywane przez widok
            Model model,
            @RequestParam("file") MultipartFile file) throws IOException {
        if(bindingResult.hasErrors()){
            Map<String, String> errorsMap = ControllerUtils.getErrors(bindingResult);
            model.mergeAttributes(errorsMap);
            model.addAttribute("message", message);
        }else{
            uploadService.transfer(file);
            message.setFilename(uploadService.getFileName());
            message.setAuthor(user);
            model.addAttribute("message", null);
            messageRepository.save(message);
        }
        Iterable<Message> messages = messageRepository.findAll();
        model.addAttribute("messages", messages);
        return "home";
    }
}
