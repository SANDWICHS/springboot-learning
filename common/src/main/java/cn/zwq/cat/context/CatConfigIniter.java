package cn.zwq.cat.context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;

import cn.zwq.cat.CatProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.util.StreamUtils;

/**
 * cat配置文件启动项目
 * 
 * @author LiQiuYu
 * @date 2019年6月17日
 */
@Configuration
@ConditionalOnProperty(value = "cat.config.enabled", havingValue = "true")
@Order(value = Integer.MIN_VALUE)
public class CatConfigIniter {

	private static final Logger logger = LoggerFactory.getLogger(CatConfigIniter.class);

	private static final String[] PATHS = new String[] { "data", "appdatas", "cat" };

	@Autowired(required = false)
	private CatProperties catProperties;

	@PostConstruct
	public void load() {
		if (catProperties != null) {
			CatSingleConfig.getConfig(catProperties.isEnabled(), catProperties.getRootPath());
		}

		init();
	}

	public static void init() {
		try {
			String rootPath = CatSingleConfig.getConfig().getRootPath();

			if (StringUtils.isNotBlank(rootPath)) {
				createConfigPath(rootPath);
				writeCatConfigFile(rootPath, "client.xml");
				writeCatConfigFile(rootPath, "datasources.xml");
				return;
			}

			/**
			 * 获取硬盘的每个盘符
			 */
			File[] fs = File.listRoots();
			// 显示磁盘卷标
			for (int i = 0; i < fs.length; i++) {
				rootPath = fs[i].getPath();
				String projectPath = System.getProperty("user.dir");
				if (StringUtils.startsWith(projectPath, rootPath)) {
					if (!createConfigPath(rootPath)) {
						logger.error("尝试生成cat的配置文件路径失败，根盘符为：{} 。", rootPath);
						continue;
					}
					writeCatConfigFile(rootPath, "client.xml");
					writeCatConfigFile(rootPath, "datasources.xml");
				}
			}
		} catch (Exception e) {
			logger.error("尝试初始化cat相关配置出错，请检查当前项目的根路径下的data/appdatas/cat目录下是否存在client.xml与datasources.xml。{}",
					e.getMessage());
		}
	}

	private static void writeCatConfigFile(String rootPath, String name) throws IOException {
		String parentPath = rootPath + File.separator + PATHS[0] + File.separator + "appdatas" + File.separator + "cat"
				+ File.separator;
		File xml = new File(parentPath + name);
		if (!xml.exists()) {
			try (FileOutputStream fos = new FileOutputStream(xml);
					// 小心
					// getResourceAsStream()这个方法有坑，如果项目打包成jar包的话，getResourceAsStream()只能访问到jar包内的资源，无法访问外部的，
					// 如果想访问外部的资源文件，推荐使用“ConfigUtils.getRunningPath()”
					InputStream fis = CatConfigIniter.class.getClassLoader().getResourceAsStream("cat/" + name)) {
				byte[] btes = StreamUtils.copyToByteArray(fis);
				fos.write(btes);
			}
		}
	}

	private static boolean createConfigPath(String rootPath) {
		StringBuilder sb = new StringBuilder();
		sb.append(rootPath);
		for (String path : PATHS) {
			sb.append(File.separator).append(path);
			File file = new File(sb.toString());
			if (!file.exists() && !file.mkdirs()) {
				return false;
			}
		}

		return true;
	}

}
