package cn.ccc212;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CheckTask {

    @Value("${wifi.name}")
    private String wifiName;

    @Autowired
    private Login login;

    //每10秒执行一次
    @Scheduled(cron = "*/10 * * * * ?")
    public void check(){
        if (!NetworkUtil.isWifiConnected()) {
            NetworkUtil.connectToWifi(wifiName);
            System.out.println(login.login());
        }
    }
}
