package cn.zwq.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class SpringBeanUtils implements ApplicationContextAware {
	private static Logger logger = LoggerFactory.getLogger(SpringBeanUtils.class);

	private static ApplicationContext applicationContext;
	private static DefaultListableBeanFactory registry;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringBeanUtils.applicationContext = applicationContext;

		ConfigurableApplicationContext configContext = (ConfigurableApplicationContext) applicationContext;
		registry = (DefaultListableBeanFactory) configContext.getBeanFactory();
	}

	public static <T> T getBean(Class<T> clazz) {
		try {
			return applicationContext.getBean(clazz);
		} catch (Exception e) {
			return applicationContext.getBean(clazz);
		}
	}

	public static Object getBean(String name) throws BeansException {
		return applicationContext.getBean(name);
	}

	public static boolean hasBean(Class<?> clazz) {
		String[] names = Optional.ofNullable(registry).map(value -> value.getBeanNamesForType(clazz)).orElse(new String[] {});
		return !(names == null || names.length == 0);
	}

	public static boolean containsBean(String name) {
		return applicationContext.containsBean(name);
	}

	public static void registerBean(Class<?> clazz, ClassLoader classLoader) {
		ConfigurableApplicationContext context = (ConfigurableApplicationContext) applicationContext;
		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
		// 通过BeanDefinitionBuilder创建bean定义
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
		BeanDefinition definition = beanDefinitionBuilder.getRawBeanDefinition();

		// 设置原型模式
		Scope scope = clazz.getAnnotation(Scope.class);
		if (scope != null) {
			definition.setScope(scope.value());
		}

		// 是否延迟加载
		Lazy lazy = clazz.getAnnotation(Lazy.class);
		if (lazy != null) {
			definition.setLazyInit(true);
		}

		// 获取beanName
		AnnotationBeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();
		String beanName = beanNameGenerator.generateBeanName(definition, beanFactory);

		// 注册bean
		beanFactory.setBeanClassLoader(classLoader);
		beanFactory.registerBeanDefinition(beanName, definition);

		SpringBeanUtils.getBean(beanName);
		logger.info("注册的className:[{}]  Scope:[{}]", clazz.getName(), definition.getScope());
	}

	public static void publishEvent(Object event) {
		applicationContext.publishEvent(event);
	}
}
