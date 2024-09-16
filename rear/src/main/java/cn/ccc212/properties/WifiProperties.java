package cn.ccc212.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wifi")
@Data
public class WifiProperties {

    private String name;

    private String username;

    private String password;

    private String srcIp;

    private String dstIp;

    private String startTime;

    private String endTime;

    private String interval;

    private String autoSelect;
}
