package pl.rmv.qwerty.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class UploadService {
    @Value("${upload.path}")
    private String uploadPath;

    private String fileName;


    public void transfer(MultipartFile file){
        boolean isEmptyFilename = file.getOriginalFilename().isEmpty();
        if(file != null && !isEmptyFilename){
            File uploadDir = new File(uploadPath);
            if(!uploadDir.exists()){
                uploadDir.mkdir();
            }
            String uuid = UUID.randomUUID().toString();
            fileName = uuid + "." + file.getOriginalFilename();

            try {
                file.transferTo(new File(uploadDir + "/" + fileName));
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public String getFileName() {
        return fileName;
    }
}
