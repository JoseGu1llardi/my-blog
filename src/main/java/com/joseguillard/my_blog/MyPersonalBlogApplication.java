package com.joseguillard.my_blog;

import com.joseguillard.my_blog.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class MyPersonalBlogApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyPersonalBlogApplication.class, args);
	}

}
