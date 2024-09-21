package cn.ccc212.core;

import cn.ccc212.pojo.BaseDTO;
import cn.ccc212.pojo.ConfigDTO;
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
    public Result start(@RequestBody BaseDTO baseDTO) {
        login.setBase(baseDTO);
        check.setNetworkName(baseDTO.getNetworkName());
        check.startTask();
        log.info("启动成功");
        return Result.success("启动成功");
    }

    @PostMapping("/stop")
    @Operation(summary = "暂停")
    public Result stop() {
        check.stopTask();
        return Result.success("暂停成功");
    }

    @PostMapping("/config")
    @Operation(summary = "配置")
    public Result config(@RequestBody ConfigDTO configDTO) {
        check.setConfig(configDTO);
        log.info("配置成功");
        return Result.success("配置成功");
    }

    @GetMapping("/clear")
    @Operation(summary = "清理日志")
    public void clear() {
        File file = new File("./cwac/application.log");
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write("\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
