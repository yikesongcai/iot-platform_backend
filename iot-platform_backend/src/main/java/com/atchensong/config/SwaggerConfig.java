//package com.atchensong.config;
//
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.info.Contact;
//import io.swagger.v3.oas.models.info.Info;
//import io.swagger.v3.oas.models.info.License;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class SwaggerConfig {
//
//    @Bean
//    public OpenAPI customOpenAPI() {
//        return new OpenAPI()
//                .info(new Info()
//                        .title("物联网设备管理平台API文档")
//                        .version("1.0")
//                        .description("基于SpringBoot的物联网设备数据管理接口")
//                        .contact(new Contact()
//                                .name("开发者")
//                                .email("dev@example.com")
//                                .url("https://your-domain.com"))
//                        .license(new License()
//                                .name("Apache 2.0")
//                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
//    }
//}
