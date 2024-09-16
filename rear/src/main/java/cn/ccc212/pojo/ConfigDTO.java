package cn.ccc212.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConfigDTO {

    private String startTime;

    private String endTime;

    private String interval;

    private String autoSelect;
}
