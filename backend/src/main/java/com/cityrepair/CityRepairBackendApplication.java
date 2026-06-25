package com.cityrepair;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.cityrepair.mapper")
@SpringBootApplication
public class CityRepairBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CityRepairBackendApplication.class, args);
    }
}
