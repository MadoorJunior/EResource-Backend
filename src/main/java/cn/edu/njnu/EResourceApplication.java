package cn.edu.njnu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

//@SpringBootApplication
//public class EResourceApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(EResourceApplication.class, args);
//	}
//
//}
@SpringBootApplication
@MapperScan("com.wky.dao.repository")
public class EResourceApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(EResourceApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(EResourceApplication.class);
	}
}
