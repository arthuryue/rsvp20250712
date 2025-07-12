package hk.jud.app.lyo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class LyoAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyoAdminApplication.class, args);
    }
}