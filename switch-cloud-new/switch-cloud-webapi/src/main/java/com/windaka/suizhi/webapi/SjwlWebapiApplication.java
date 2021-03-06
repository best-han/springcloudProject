package com.windaka.suizhi.webapi;

import com.windaka.suizhi.common.utils.ElasticSearchUtil;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class SjwlWebapiApplication {

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("config.yml"));
        configurer.setProperties(yaml.getObject());
        return configurer;
    }

    public static void main(String[] args) {
        ElasticSearchUtil.getInitClient();
        SpringApplication.run(SjwlWebapiApplication.class, args);
    }
}
