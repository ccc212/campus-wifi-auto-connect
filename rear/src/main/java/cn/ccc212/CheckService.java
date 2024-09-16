package cn.ccc212;

import cn.ccc212.core.Login;
import cn.ccc212.pojo.ConfigDTO;
import cn.ccc212.utils.NetworkUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

@Service
public class CheckService {

    @Value("${wifi.name}")
    private String wifiName;

    @Autowired
    private Login login;
    @Autowired
    private TaskScheduler taskScheduler;
    private ScheduledFuture<?> futureTask;
    private LocalTime startTime;
    private LocalTime endTime;
    private long interval = 10 * 1000; // 默认任务执行间隔10秒

    // 更新任务的启动和停止时间以及执行间隔
    public void configureTask(ConfigDTO configDTO) {
        String startTime = configDTO.getStartTime();
        String endTime = configDTO.getEndTime();
        String interval = configDTO.getInterval();
        this.startTime = LocalTime.parse(startTime);  // 解析启动时间
        this.endTime = LocalTime.parse(endTime);      // 解析结束时间
        this.interval = Long.parseLong(interval) * 1000;  // 转换为毫秒
    }

    // 定时监测是否需要开启或关闭任务（每分钟检查一次）
    @Scheduled(cron = "0 * * * * ?")  // 每分钟执行一次
    public void monitorTask() {
        LocalTime now = LocalTime.now();
        if (now.isAfter(startTime) && now.isBefore(endTime)) {
            // 当前时间在任务区间内，确保任务在运行
            startTask();
        } else {
            // 当前时间不在任务区间，停止任务
            stopTask();
        }
    }

    @SneakyThrows
    public void startTask() {
        stopTask();

        // 当前日期
        LocalDate today = LocalDate.now();

        // 将startTime和endTime转换为当天的LocalDateTime
        LocalDateTime startDateTime = LocalDateTime.of(today, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(today, endTime);

        // 当前时间
        LocalDateTime now = LocalDateTime.now();

        // 计算启动时间和结束时间的延迟（毫秒）
        long startDelay = ChronoUnit.MILLIS.between(now, startDateTime);
        long endDelay = ChronoUnit.MILLIS.between(now, endDateTime);

        if (startDelay > 0) {
            futureTask = taskScheduler.scheduleAtFixedRate(this::check, 
                    new Date(System.currentTimeMillis() + startDelay), 
                    interval);
            System.out.println("任务将在 " + startTime + " 启动");
        }

        if (endDelay > 0 && futureTask != null) {
            //在结束时间停止任务
            taskScheduler.schedule(() -> stopTask(), 
                    new Date(System.currentTimeMillis() + endDelay));
            System.out.println("任务将在 " + endTime + " 停止");
        }
    }

    public void stopTask() {
        if (futureTask != null && !futureTask.isCancelled()) {
            futureTask.cancel(true);
        }
    }

    private void check() {
        try {
            String ssid = NetworkUtil.getSSID(NetworkUtil.checkWifiStatus());
            boolean wifiConnected = NetworkUtil.isWifiConnected();
            System.out.println("ssid = " + ssid);
            System.out.println("wifiConnected = " + wifiConnected);
            if (ssid.equals(wifiName) && !wifiConnected) {
                System.out.println(login.login(null));
            }
            else if (!ssid.equals(wifiName) && !wifiConnected) {
                NetworkUtil.connectToWifi(wifiName);
                System.out.println(login.login(null));
            }
        } catch (Exception e) {
            throw new RuntimeException("连接异常");
        }
    }
}
