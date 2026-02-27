package com.twsela.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Configuration for Twsela Courier System
 * Provides comprehensive API documentation with authentication support
 */
@Configuration
public class SwaggerConfig {

    @Value("${app.swagger.dev-server-url:http://localhost:8080}")
    private String devServerUrl;

    @Value("${app.swagger.prod-server-url:https://api.twsela.com}")
    private String prodServerUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Twsela Courier System API")
                        .description("""
                                ## نظام تسيلا للشحن السريع
                                
                                نظام شامل لإدارة الشحنات والخدمات اللوجستية مع دعم كامل للغة العربية.
                                
                                ### المميزات الرئيسية:
                                - إدارة الشحنات الكاملة
                                - تتبع الشحنات في الوقت الفعلي
                                - إدارة المستخدمين والأدوار
                                - التقارير المالية والإحصائية
                                - إدارة المستودعات والمانيفست
                                - نظام الدفع والمكافآت
                                - الإشعارات عبر الرسائل النصية
                                - النسخ الاحتياطي والمراجعة
                                
                                ### الأدوار المدعومة:
                                - **OWNER**: مالك النظام - صلاحيات كاملة
                                - **ADMIN**: مدير النظام - إدارة شاملة
                                - **MERCHANT**: تاجر - إدارة شحناته
                                - **COURIER**: عامل توصيل - إدارة مهامه
                                - **WAREHOUSE_MANAGER**: مدير مستودع - إدارة المخزون
                                
                                ### المصادقة:
                                جميع الطلبات تتطلب مصادقة باستخدام JWT Token.
                                استخدم endpoint `/api/auth/login` للحصول على التوكن.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Twsela Development Team")
                                .email("dev@twsela.com")
                                .url("https://twsela.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url(devServerUrl)
                                .description("Development Server"),
                        new Server()
                                .url(prodServerUrl)
                                .description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\"")));
    }
}
