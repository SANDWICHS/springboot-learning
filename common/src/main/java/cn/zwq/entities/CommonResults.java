package cn.zwq.entities;

/**
 * @author zhangwenqia
 * @create 2022-01-19 18:21
 */
public class CommonResults<T> {
	public CommonResults() {
	}

	public CommonResults(Integer code, String message) {
		setCode(code);
		setMessage(message);
	}

	public CommonResults(Integer code, String message, T datas) {
		setCode(code);
		setMessage(message);
		setDatas(datas);
	}

	private Integer code = 200;
	private String message = "成功";
	private T datas;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getDatas() {
		return datas;
	}

	public void setDatas(T datas) {
		this.datas = datas;
	}
}
