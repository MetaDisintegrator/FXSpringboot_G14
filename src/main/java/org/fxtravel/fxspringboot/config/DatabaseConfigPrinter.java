package org.fxtravel.fxspringboot.config;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConfigPrinter implements ApplicationRunner {

    private final Environment env;

    public DatabaseConfigPrinter(Environment env) {
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) {
        String url = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");

        // 密码脱敏处理
        String maskedPassword = password != null ? password.replaceAll(".", "*") : "";

        // 直接输出到控制台
        System.out.println("\n\n====== Database Configuration ======");
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);
        System.out.println("Password: " + maskedPassword);
        System.out.println("====================================\n");
    }
}
