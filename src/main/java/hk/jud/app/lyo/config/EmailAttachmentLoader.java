package hk.jud.app.lyo.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import jakarta.annotation.PostConstruct;

@Component
public class EmailAttachmentLoader {

    private Map<String, Map<String, Map<String, List<String>>>> attachments;

    @PostConstruct
    public void loadYaml() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        InputStream inputStream = new ClassPathResource("email-attachments.yml").getInputStream();

        Map<String, Object> root = mapper.readValue(inputStream, Map.class);
        this.attachments = (Map<String, Map<String, Map<String, List<String>>>>)
                ((Map<String, Object>) root.get("email")).get("attachments");

        System.out.println("✅ Email attachments loaded: " + attachments.keySet());
    }

    public List<String> getAttachments(String userType, String emailType, String lang) {
        return attachments.getOrDefault(userType, Map.of())
                          .getOrDefault(emailType, Map.of())
                          .getOrDefault(lang, List.of());
    }
   
}
