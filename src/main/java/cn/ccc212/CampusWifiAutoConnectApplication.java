package cn.ccc212;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CampusWifiAutoConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusWifiAutoConnectApplication.class, args);
    }

}
