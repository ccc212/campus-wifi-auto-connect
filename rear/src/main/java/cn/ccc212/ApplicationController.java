package cn.ccc212;

import cn.ccc212.core.Login;
import cn.ccc212.pojo.BaseDTO;
import cn.ccc212.pojo.ConfigDTO;
import cn.ccc212.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "接口")
public class ApplicationController {

    @Autowired
    private Login login;

    @PostMapping("/start")
    @Operation(summary = "启动")
    public Result start(@RequestBody BaseDTO baseDTO) {
        login.login(baseDTO);
        return Result.success();
    }

    @PostMapping("/stop")
    @Operation(summary = "暂停")
    public Result stop() {
        return Result.success();
    }

    @PostMapping("/config")
    @Operation(summary = "配置")
    public Result config(@RequestBody ConfigDTO configDTO) {
        return Result.success();
    }


}
