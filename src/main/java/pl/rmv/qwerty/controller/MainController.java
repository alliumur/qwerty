package pl.rmv.qwerty.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import pl.rmv.qwerty.model.Message;
import pl.rmv.qwerty.model.Role;
import pl.rmv.qwerty.model.User;
import pl.rmv.qwerty.repository.MessageRepository;
import pl.rmv.qwerty.service.UploadService;
import pl.rmv.qwerty.service.UserService;
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

    @Autowired
    private UserService userService;

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

    @PostMapping("/user-messages/{username}/createmessage")
    public String createmessage(
            @PathVariable String username,
            @AuthenticationPrincipal User user,
            @Valid Message message,
            BindingResult bindingResult, // zawsze musi być przed 'model', żeby błędy nie były przechwytywane przez widok
            Model model,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
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
        return "redirect:/user-messages/"+username;
    }

    @GetMapping("/user-messages/{username}")
    public String messages(
            @PathVariable String username,
            @AuthenticationPrincipal User currentUser,
            Model model,
            @RequestParam(required = false) Message message
    ){
        User user = userService.getUser(username);
        model.addAttribute("editable", currentUser.getUsername().equals(username));
        model.addAttribute("username", username);
        model.addAttribute("isSubscribe", user.getSubscribers().contains(currentUser));
        model.addAttribute("subscriptionsCount", user.getSubscriptions().size());
        model.addAttribute("subscribersCount", user.getSubscribers().size());
        model.addAttribute("message",  message);
        model.addAttribute("messages",  user.getMessages());
        return "user-messages";
    }

    @PostMapping("/user-messages/{username}")
    public String updateMessages(
            @PathVariable String username,
            @AuthenticationPrincipal User currentUser,
            @RequestParam("id") Message message,
            @RequestParam("text") String text,
            @RequestParam("tag") String tag,
            @RequestParam("file") MultipartFile file

    ){
        if(message.getAuthor().equals(currentUser)){
            if(!StringUtils.isEmpty(text)){
                message.setText(text);
            }
            if(!StringUtils.isEmpty(tag)){
                message.setTag(tag);
            }

            uploadService.transfer(file);
            message.setFilename(uploadService.getFileName());
            messageRepository.save(message);
        }
        return "redirect:/user-messages/"+username;
    }
}
