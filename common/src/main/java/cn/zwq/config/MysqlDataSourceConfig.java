package cn.zwq.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * @author zhangwenqia
 * @create 2023-03-23 17:30
 * @description 类描述
 */
@Configuration
@ConditionalOnExpression("${spring.datasource.mysql.enable:false}")
public class MysqlDataSourceConfig {
	/**
	 * 数据源
	 *
	 * @return 数据源
	 */
	@Bean(name = "mysqlDataSource")
	@Primary
	public DataSource mysqlDataSource(
			@Qualifier("mysqlDataSourceProperties") DataSourceProperties mysqlDataSourceProperties) {
		DruidDataSource datasource = new DruidDataSource();
		datasource.setUrl(mysqlDataSourceProperties.getUrl());
		datasource.setDriverClassName(mysqlDataSourceProperties.getDriverClassName());
		datasource.setUsername(mysqlDataSourceProperties.getUsername());
		datasource.setPassword(mysqlDataSourceProperties.getPassword());
		datasource.setMinIdle(1);// 连接池中的最小空闲连接数
		datasource.setMaxActive(1);// 线程池中最大连接数
		datasource.setInitialSize(1);// 同时连接池内部有机制判断，如果当前的总的连接数少于miniIdle，则会建立新的空闲连接，以保证连接数得到miniIdle。
		datasource.setRemoveAbandoned(true);// 如果连接泄露，是否需要回收泄露的连接
		datasource.setRemoveAbandonedTimeout(8);// 设置druid 强制回收连接的时限，当程序从池中get到连接开始算起，druid将强制回收该连接，单位秒
		datasource.setLogAbandoned(true);// 如果回收了泄露的连接，是否要打印一条log
		datasource.setPoolPreparedStatements(false);// .7之后支持游标，是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大；
		datasource.setMaxPoolPreparedStatementPerConnectionSize(20);// 每个连接最多缓存多少个SQL；
		datasource.setTimeBetweenEvictionRunsMillis(6);// 判断连接池的连接空闲数是否大于minIdle，如果是则关闭多余的连接数，少的就补上，（如果当前连接池中某个连接在空闲了timeBetweenEvictionRunsMillis时间后仍然没有使用，则被物理性的关闭掉）；
		datasource.setValidationQuery("select 1");// 用来检测连接是否有效的sql，如果validationQuery为空，那么testOnBorrow、testOnReturn、testWhileIdle这三个参数都不会起作用，因为这三个参数都是通过执行参数validationQuery指定的SQL来验证数据库连接的有效性，配置参考validationQuery=SELECT
													// 1
		datasource.setTestOnBorrow(false);// 建议配置为false，申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。
		datasource.setTestWhileIdle(true);// 是一个检测配置项：建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效；
		datasource.setTestOnReturn(false);// 建议配置为false，归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能；
		return datasource;
	}

	@Bean(name = "mysqlJdbcTemplate")
	@Primary
	public JdbcTemplate mysqlJdbcTemplate(@Qualifier("mysqlDataSource") DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean("mysqlTransactionManager")
	@Primary
	public DataSourceTransactionManager getDataSourceTransactionManager(@Qualifier("mysqlDataSource") DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	/**
	 * mysql 数据库连接配置
	 *
	 * @return 数据库连接配置
	 */
	@Bean(name = "mysqlDataSourceProperties")
	@ConfigurationProperties(prefix = "spring.datasource.mysql")
	@Primary
	public DataSourceProperties mysqlDataSourceProperties() {
		return new DataSourceProperties();
	}
}
