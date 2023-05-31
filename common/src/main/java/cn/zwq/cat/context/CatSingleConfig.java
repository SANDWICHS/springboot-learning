package cn.zwq.cat.context;

import cn.zwq.cat.CatProperties;
import cn.zwq.util.SpringBeanUtils;

public class CatSingleConfig {

	private static CatSingleConfig config = null;

	private boolean enable;

	private String rootPath;

	private static boolean running = false;

	public static boolean isRunning() {
		return running;
	}

	public static void setRunning(boolean run) {
		running = run;
	}

	private static volatile boolean init = false;

	private CatSingleConfig(boolean catEnable, String catRootPath) {
		enable = catEnable;
		rootPath = catRootPath;
	}

	public static CatSingleConfig getConfig() {
		if (init) {
			return config;
		} else {
			CatProperties catProperties = SpringBeanUtils.getBean(CatProperties.class);
			if (catProperties != null) {
				return getConfig(catProperties.isEnabled(), catProperties.getRootPath());
			} else {
				return getConfig(false, "");

			}
		}
	}

	static CatSingleConfig getConfig(boolean catEnable, String rootPath) {
		if (!init) {
			synchronized (CatSingleConfig.class) {
				if (!init) {
					config = new CatSingleConfig(catEnable, rootPath);
					init = true;
				}
			}
		}
		return config;
	}

	public boolean enable() {
		return this.enable;
	}

	public String getRootPath() {
		return rootPath;
	}

}
