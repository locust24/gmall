package com.atguigu.gmall.search;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableDubbo
@MapperScan(basePackages = "com.auguigu.gmall")
public class GmallSearchServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallSearchServiceApplication.class, args);
	}

}
