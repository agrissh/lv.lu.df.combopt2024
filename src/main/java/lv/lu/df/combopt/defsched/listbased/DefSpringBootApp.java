package lv.lu.df.combopt.defsched.listbased;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class DefSpringBootApp {
    public static void main(String[] args) {
        SpringApplication.run(DefSpringBootApp.class, args);
    }

    /*public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/defsched/**");
            }
        };
    }*/
}
