package cn.zwq.util;

import javax.servlet.http.HttpServletRequest;

import cn.zwq.constant.SessionConstants;
import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author dell
 *
 */
public class RequestTokenGetUtils {

	private RequestTokenGetUtils() {

	}

	public static String getTokenFromRequest(HttpServletRequest request) {
		String token = request.getParameter(SessionConstants.TOKEN);
		if (StringUtils.isBlank(token)) {
			token = request.getHeader(SessionConstants.TOKEN);
		}
		return token;
	}

}
