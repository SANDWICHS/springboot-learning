package cn.zwq.config;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class MachineInfo {
	private static Logger logger = LoggerFactory.getLogger(MachineInfo.class);

	@Value("${server.port:123}")
	private String port;

	public String getInfo() {
		return getLocalMac() + ":" + port;
	}

	public static void main(String[] args) {
		logger.info(new MachineInfo().getLocalMac());
	}

	/**
	 * 返回一个物理地址，不包含分隔符
	 *
	 * @return
	 */
	private String getLocalMac() {
		try {
			Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
			while (enumeration.hasMoreElements()) {
				NetworkInterface networkInterface = enumeration.nextElement();
				String mac = getMacString(networkInterface);
				if (mac != null) {
					return mac;
				}
			}
		} catch (SocketException e) {
			logger.error("获取服务器物理地址发生异常", e);
		}
		throw new NullPointerException("获取物理地址失败！");
	}

	private String getMacString(NetworkInterface networkInterface) throws SocketException {
		if (networkInterface != null) {
			byte[] bytes = networkInterface.getHardwareAddress();
			if (bytes != null) {
				StringBuilder stringBuffer = new StringBuilder();
				for (int i = 0; i < bytes.length; i++) {
					int tmp = bytes[i] & 0xff; // 字节转换为整数
					String str = Integer.toHexString(tmp);
					if (str.length() == 1) {
						stringBuffer.append("0".concat(str));
					} else {
						stringBuffer.append(str);
					}
				}
				String mac = stringBuffer.toString().toUpperCase();
				if (validMac(mac)) {
					return mac;
				}
			}
		}
		return null;
	}

	private boolean validMac(String mac) {
		if (mac == null) {
			return false;
		}
		if (mac.length() == 0) {
			return false;
		} else if (mac.length() == 12 && !"000000000000".equals(mac)) {
			return true;
		} else if (mac.length() == 16 && !"00000000000000E0".equals(mac)) {
			return true;
		}
		return true;
	}
}
