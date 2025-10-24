package com.ruoyi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 *
 * @author ruoyi
 */
@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class RuoYiApplication {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        SpringApplication.run(RuoYiApplication.class, args);
        log.info("=====================================================");
        log.info("====================恭喜！启动成功。====================耗时：{}ms", System.currentTimeMillis() - start);
        log.info("=====================================================");
    }
}
