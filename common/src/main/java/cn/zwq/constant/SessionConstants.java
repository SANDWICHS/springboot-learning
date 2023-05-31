package cn.zwq.constant;

/**
 * session 模块的常量类
 * 
 * @author dell
 *
 */
public class SessionConstants {

	private SessionConstants() {

	}

	/**
	 * 用户登录后前端请求访问后端携带的待校验内容的名称
	 */
	public static final String TOKEN = "token";

	/**
	 * 
	 */
	public static final String USER_UNKNOW_SQL_EXECUTE_DEBUG_FLAG = "user_unknow_sql_execute_debug_flag";

	/**
	 * 
	 */
	public static final String USER_EXTRA_DATA_REPOSITORY = "user_extra_data";

	/**
	 * 
	 */
	public static final String USER_WEB_EXTAR_DATA_REPOSITORY = "user_web_extra_data";

	/**
	 * 
	 */
	public static final String USER_WEB_AREA_EXTAR_DATA_REPOSITORY = "user_web_area_extra_data";

	/**
	 * 
	 */
	public static final String TEMP_SESSION_PREFIX = "temp_session_info";

	public static final String USER_OWN_APP = "user_own_app";
	public static final String USER_OWN_APP_ALL_KEYS = "user_own_app_all_keys";

	public static String getUserOwnAppKey(int userId) {
		return USER_OWN_APP + ":" + userId;
	}

}
