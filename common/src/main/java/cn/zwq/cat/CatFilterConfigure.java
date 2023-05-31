package cn.zwq.cat;

import cn.zwq.filter.CustomCatFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "cat.config.enabled", havingValue = "true")
public class CatFilterConfigure {

	@Bean
	public FilterRegistrationBean<CustomCatFilter> catFilter() {
		FilterRegistrationBean<CustomCatFilter> registration = new FilterRegistrationBean<>();
		CustomCatFilter filter = new CustomCatFilter();
		registration.setFilter(filter);
		registration.addUrlPatterns("/*");
		registration.setName("customCatFilter");
		registration.setOrder(1);
		return registration;
	}
}