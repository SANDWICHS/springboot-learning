package cn.zwq.conf;

import lombok.Getter;
import lombok.Setter;

public class FtpProperties {
    @Setter
	@Getter
    private String ip;
    @Setter
	@Getter
    private Integer port;
    @Setter
	@Getter
    private Integer timeout;
    @Setter
	@Getter
    private String userName;
    @Setter
	@Getter
    private String miMa;
    @Setter
	@Getter
    private String encoding;
    @Setter
	@Getter
    private String remotePath;
}
