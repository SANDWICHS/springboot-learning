package cn.zwq.conf;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "order")
@RefreshScope
public class CallProperties {
	private Map<String, FtpProperties> ftps = new HashMap<>();
    public Map<String, FtpProperties> getFtps() {
		return ftps;
	}

	public void setFtps(Map<String, FtpProperties> ftps) {
		this.ftps = ftps;
	}

	public FtpProperties getFtpProperties(String prefix) {
		return ftps.get(prefix);
	}
}