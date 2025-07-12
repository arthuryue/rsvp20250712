package hk.jud.app.lyo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Component
public class GuestTypeLoader {

    @Value("${rsvp-config-file:guest-types.yml}")
    private String configFile;

    private Map<String, Map<String, Boolean>> guestRsvpConfig;

    @PostConstruct
    public void loadYaml() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        InputStream inputStream = new ClassPathResource(configFile).getInputStream();

        Map<String, Object> root = mapper.readValue(inputStream, Map.class);
        this.guestRsvpConfig = (Map<String, Map<String, Boolean>>) root.get("guest_type");

        System.out.println("✅ Guest RSVP configuration loaded. Types: " + guestRsvpConfig.keySet());
    }

    public boolean hasRsvp(String guestType, String guestSubType) {
        return guestRsvpConfig.getOrDefault(guestType, Map.of())
                            .getOrDefault(guestSubType, false);
    }

    public Map<String, Boolean> getRsvpStatusForType(String guestType) {
        return guestRsvpConfig.getOrDefault(guestType, Map.of());
    }

    public Map<String, Map<String, Boolean>> getAllRsvpConfig() {
        return guestRsvpConfig;
    }
}