package cn.zwq.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import ru.yandex.clickhouse.BalancedClickhouseDataSource;
import ru.yandex.clickhouse.ClickHouseDataSource;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangwenqia
 * @create 2023-03-20 17:38
 * @description 类描述
 */

@Configuration
@ConditionalOnExpression("${spring.datasource.click.enable:false}")
public class ClickHouseDataSourcesConfig {
	/**
	 * 单机版 ch 数据源
	 * 
	 * @return 数据源
	 */
	@Bean(name = "clickHouseDataSource")
	public DataSource clickHouseDataSource(
			@Qualifier("clickHouseDataSourceProperties") ClickHouseDataSourceProperties clickHouseDataSourceProperties) {
		if (clickHouseDataSourceProperties.isBalance()) {
			/**
			 * 集群版 ch 数据源
			 *
			 * @return 数据源
			 */
			BalancedClickhouseDataSource balancedClickhouseDataSource = new BalancedClickhouseDataSource(clickHouseDataSourceProperties.getUrl(),
					clickHouseDataSourceProperties); // 配置定时任务定时检查可用的数据库连接
			balancedClickhouseDataSource.scheduleActualization(10, TimeUnit.SECONDS);
			return balancedClickhouseDataSource;
		}
		return new ClickHouseDataSource(clickHouseDataSourceProperties.getUrl(), clickHouseDataSourceProperties);
	}

	@Bean(name = "clickHouseJdbcTemplate")
	public JdbcTemplate mysqlJdbcTemplate(@Qualifier("clickHouseDataSource") DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean("clickHouseTransactionManager")
	public DataSourceTransactionManager getDataSourceTransactionManager(@Qualifier("clickHouseDataSource") DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	/**
	 * ch 数据库连接配置
	 * 
	 * @return 数据库连接配置
	 */
	@Bean(name = "clickHouseDataSourceProperties")
	@ConfigurationProperties(prefix = "spring.datasource.click")
	public ClickHouseDataSourceProperties clickHouseDataSourceProperties() {
		return new ClickHouseDataSourceProperties();
	}
}
