package cn.zwq.cat;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "cat.config")
public class CatProperties {

	private String rootPath;

	private boolean enabled;

	private String traceId;

	private List<String> services;

	private String clientSystem;

	private String domain;

	private List<String> logFilterPrefixes;

	private List<String> allInOnePaths;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public List<String> getServices() {
		return services;
	}

	public void setServices(List<String> services) {
		this.services = services;
	}

	public String getClientSystem() {
		return clientSystem;
	}

	public void setClientSystem(String clientSystem) {
		this.clientSystem = clientSystem;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public List<String> getLogFilterPrefixes() {
		return logFilterPrefixes;
	}

	public void setLogFilterPrefixes(List<String> logFilterPrefixes) {
		this.logFilterPrefixes = logFilterPrefixes;
	}

	public List<String> getAllInOnePaths() {
		return allInOnePaths;
	}

	public void setAllInOnePaths(List<String> allInOnePaths) {
		this.allInOnePaths = allInOnePaths;
	}
}
