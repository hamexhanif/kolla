package team5.prototype;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

class KollaBackendApplicationMainTest {

    @Test
    void mainStartsAndStopsContext() {
        String[] args = new String[] { "--spring.profiles.active=main-test", "--spring.main.web-application-type=none" };

        KollaBackendApplication.main(args);

        ConfigurableApplicationContext context = MainTestContextHolder.getContext();
        assertThat(context).isNotNull();
        context.close();
    }
}

@Component
@Profile("main-test")
class MainTestContextHolder implements ApplicationContextAware {

    private static volatile ConfigurableApplicationContext context;

    static ConfigurableApplicationContext getContext() {
        return context;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            context = (ConfigurableApplicationContext) applicationContext;
        }
    }
}
