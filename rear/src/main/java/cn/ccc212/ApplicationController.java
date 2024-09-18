package cn.ccc212;

import cn.ccc212.core.Login;
import cn.ccc212.pojo.BaseDTO;
import cn.ccc212.pojo.ConfigDTO;
import cn.ccc212.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "接口")
@Slf4j
public class ApplicationController {

    @Autowired
    private Login login;
    @Autowired
    private CheckService checkService;

    @PostMapping("/start")
    @Operation(summary = "启动")
    public Result start(@RequestBody BaseDTO baseDTO) {
        login.setBase(baseDTO);
        checkService.startTask();
        log.info("启动成功");
        return Result.success("启动成功");
    }

    @PostMapping("/stop")
    @Operation(summary = "暂停")
    public Result stop() {
        checkService.stopTask();
        log.info("暂停成功");
        return Result.success("暂停成功");
    }

    @PostMapping("/config")
    @Operation(summary = "配置")
    public Result config(@RequestBody ConfigDTO configDTO) {
        checkService.setConfig(configDTO);
        log.info("配置成功");
        return Result.success("配置成功");
    }


}
