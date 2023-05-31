package cn.zwq.cat;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CatProperties.class)
public class CatAutoConfiguration {

}
