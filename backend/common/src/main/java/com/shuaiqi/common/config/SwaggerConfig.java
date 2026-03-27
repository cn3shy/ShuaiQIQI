package com.shuaiqi.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 配置
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("帅气气 API 文档")
                        .description("帅气气项目后端接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("帅气气团队")
                                .email("admin@shuaiqi.com")
                        )
                );
    }
}
