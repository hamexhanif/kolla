package team5.prototype.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class H2ConsoleConfiguration implements WebMvcConfigurer {

    @Bean
    @ConditionalOnProperty(name = "spring.h2.console.enabled", havingValue = "true")
    public ServletRegistrationBean<?> h2Console() {
        String path = "/h2-console";
        String urlMapping = path + "/*";

        ServletRegistrationBean<?> registrationBean = new ServletRegistrationBean<>(
                new org.h2.server.web.JakartaWebServlet(), urlMapping);
        registrationBean.setLoadOnStartup(1);

        return registrationBean;
    }
}