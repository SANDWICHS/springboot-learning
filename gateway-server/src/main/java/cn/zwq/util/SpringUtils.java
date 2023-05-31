package cn.zwq.util;

import java.lang.reflect.Modifier;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Component
public class SpringUtils implements ApplicationContextAware {

	private static ApplicationContext context;

	private static DefaultListableBeanFactory registry;

	private static AnnotationBeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

	private static ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

	private static Logger logger = LoggerFactory.getLogger(SpringUtils.class);

	private static void afterProperties(ApplicationContext applicationContext) {
		ConfigurableApplicationContext configContext = (ConfigurableApplicationContext) applicationContext;

		context = applicationContext;
		registry = (DefaultListableBeanFactory) configContext.getBeanFactory();

	}

	public static void reload(ApplicationContext ac, DefaultListableBeanFactory rg) {
		context = ac;
		registry = rg;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		afterProperties(applicationContext);
	}

	public static <T> T getBean(Class<T> clazz) {
		return Optional.ofNullable(context).map(value -> value.getBean(clazz)).orElse(null);
	}

	public static Object getBean(String name) {
		return Optional.ofNullable(context).map(value -> value.getBean(name)).orElse(null);
	}

	public static boolean hasBean(Class<?> clazz) {
		String[] names = Optional.ofNullable(registry).map(value -> value.getBeanNamesForType(clazz)).orElse(new String[] {});
		return names.length != 0;
	}

	public static boolean hasBean(String name) {
		return Optional.ofNullable(context).map(value -> value.containsBean(name)).orElse(false);
	}

	public static void registerBean(Class<?> clazz) {
		registerBean(clazz, null, null);
	}

	public static void registerBean(Class<?> clazz, String beanName) {
		registerBean(clazz, beanName, null);
	}

	public static void registerBean(Class<?> clazz, ClassLoader classLoader) {
		registerBean(clazz, null, classLoader);
	}

	public static void registerBean(Class<?> clazz, String beanName, ClassLoader classLoader) {

		AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(clazz);
		ScopeMetadata scopeMetadata = scopeMetadataResolver.resolveScopeMetadata(abd);
		abd.setScope(scopeMetadata.getScopeName());

		if (StringUtils.isBlank(beanName)) {
			beanName = beanNameGenerator.generateBeanName(abd, registry);
		}

		if (classLoader != null) {
			registry.setBeanClassLoader(classLoader);
		}

		AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);

		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);

		SpringUtils.getBean(beanName);

		logger.info("注册的className:[{}]  Scope:[{}]", clazz.getName(), scopeMetadata.getScopeName());
	}

	/**
	 * 方法描述 判断class对象是否带有spring的注解
	 * 
	 * @method isSpringBean
	 * @param clazz
	 * @return true 是spring bean false 不是spring bean
	 */
	public static boolean isSpringBean(Class<?> clazz) {
		if (clazz == null) {
			return false;
		}
		// 是否是接口
		if (clazz.isInterface()) {
			return false;
		}

		// 是否是抽象类
		if (Modifier.isAbstract(clazz.getModifiers())) {
			return false;
		}
		// 是否Spring注释类
		return clazz.getAnnotation(Component.class) != null || clazz.getAnnotation(Repository.class) != null
				|| clazz.getAnnotation(Service.class) != null;

	}
}
