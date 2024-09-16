package cn.ccc212;

import cn.ccc212.utils.NetworkUtil;
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
        try {
            String ssid = NetworkUtil.getSSID(NetworkUtil.checkWifiStatus());
            boolean wifiConnected = NetworkUtil.isWifiConnected();
            System.out.println("ssid = " + ssid);
            System.out.println("wifiConnected = " + wifiConnected);
            if (ssid.equals(wifiName) && !wifiConnected) {
                System.out.println(login.login());
            }
            else if (!ssid.equals(wifiName) && !wifiConnected) {
                NetworkUtil.connectToWifi(wifiName);
                System.out.println(login.login());
            }
        } catch (Exception e) {
            throw new RuntimeException("connect error");
        }
    }
}
