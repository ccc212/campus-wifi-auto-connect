package cn.ccc212.core;

import cn.ccc212.pojo.BaseDTO;
import cn.ccc212.pojo.ConfigDTO;
import cn.ccc212.pojo.GlobalState;
import cn.ccc212.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@RestController
@Tag(name = "接口")
@Slf4j
public class ApplicationController {

    @Autowired
    private Login login;
    @Autowired
    private Check check;

    @PostMapping("/start")
    @Operation(summary = "启动")
    public synchronized Result start(@RequestBody BaseDTO baseDTO) {
        login.setBase(baseDTO);
        check.setNetworkName(baseDTO.getNetworkName());
        String result = check.startTask();
        log.info(result);
        return result.equals("启动成功") ? Result.success(result) : Result.error(result);
    }

    @PostMapping("/stop")
    @Operation(summary = "暂停")
    public synchronized Result stop() {
        check.stopTask();
        return Result.success("暂停成功");
    }

    @PostMapping("/config")
    @Operation(summary = "配置")
    public synchronized Result config(@RequestBody ConfigDTO configDTO) {
        String result = check.setConfig(configDTO);
        return Result.success(result);
    }

}
