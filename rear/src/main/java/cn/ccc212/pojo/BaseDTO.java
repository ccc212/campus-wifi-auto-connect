package cn.ccc212.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BaseDTO {

    private String networkName;

    private String username;

    private String password;

    private String dstIp;

    private String acId;
}
