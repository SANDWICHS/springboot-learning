package cn.zwq.http;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Optional;

import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;

import cn.zwq.cat.context.CatContext;
import cn.zwq.cat.util.CatServiceLogUtils;
import cn.zwq.cat.util.CatUtils;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.unidal.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

import cn.hutool.http.webservice.SoapClient;
import cn.hutool.http.webservice.SoapProtocol;

/**
 * @author zhangwenqia
 * @create 2022-03-26 14:53
 * @description 类描述
 */
public class WebServiceUtil {
	static Logger logger = org.slf4j.LoggerFactory.getLogger(WebServiceUtil.class);

	private WebServiceUtil() {
	}

	public static Object[] executeWebService(Pair<String, String> wsdlUrl, String method, Integer timeout, Object... params) {
		Transaction t = Cat.newTransaction("WebService", wsdlUrl.getKey());
		for (int i = 0; i < params.length; i++) {
			logger.info("param-{}:{}", i, params[i]);
		}

		try {
			timeout = Optional.ofNullable(timeout).orElse(120000);
			JaxWsDynamicClientFactory factory = JaxWsDynamicClientFactory.newInstance();
			Client client = factory.createClient(wsdlUrl.getKey());
			// client.getOutInterceptors().add(new ClientLoginInterceptor("admin",
			// "123456"));

			HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
			httpClientPolicy.setConnectionTimeout(timeout.longValue());// 连接超时
			httpClientPolicy.setAllowChunking(false);// 取消快编码
			httpClientPolicy.setReceiveTimeout(timeout.longValue());// 相应超时时间

			HTTPConduit conduit = (HTTPConduit) client.getConduit();
			conduit.setClient(httpClientPolicy);
			if (StringUtils.isNotEmpty(wsdlUrl.getValue())) {
				logger.info("targetUrl:{}", wsdlUrl.getValue());
				conduit.getTarget().getAddress().setValue(wsdlUrl.getValue());
			}

			Object[] result = client.invoke(method, params);
			t.setSuccessStatus();
			return result;
		} catch (Exception e) {
			logger.error("executeWebService调用异常,wsdlURL = {};method = {};parameterValues = {}", wsdlUrl.getKey(), method, params);
			Cat.getProducer().logError(e);
			t.setStatus(e);
			throw new RuntimeException(e);
		} finally {
			t.complete();
		}
	}

	public static String doPostSoap(String url, String soap, String SOAPAction, Integer timeout, SoapProtocol soapProtocol) {
		Transaction t = CatUtils.newTransaction("WebService", url);
		t.setSuccessStatus();
		logger.info("soap:{}", soap);
		// 请求体
		HttpPost httpPost = new HttpPost(url);

		CatContext context = new CatContext();
		Cat.logRemoteCallClient(context, CatServiceLogUtils.getDomain());
		String catId = context.getProperty(Cat.Context.CHILD);
		String parentId = context.getProperty(Cat.Context.PARENT);
		String catRootId = context.getProperty(Cat.Context.ROOT);
		httpPost.addHeader(CatServiceLogUtils.KEY_CHILD, catId);
		httpPost.addHeader(CatServiceLogUtils.KEY_PARENT, parentId);
		httpPost.addHeader(CatServiceLogUtils.KEY_ROOT, catRootId);
		// httpPost.addHeader(CatServiceLogUtils.KEY_TRACE_MODE, "true");
		httpPost.addHeader(CatServiceLogUtils.KEY_CLIENT_SYSTEM, CatServiceLogUtils.getClientSystem());

		// 创建HttpClientBuilder
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		// HttpClient
		try (CloseableHttpClient closeableHttpClient = httpClientBuilder.build()) {
			httpPost.setHeader("Content-Type", "text/xml;charset=UTF-8");
			httpPost.setHeader("SOAPAction", SOAPAction);
			StringEntity data = new StringEntity(soap, Charset.forName("UTF-8"));
			httpPost.setEntity(data);
			timeout = Optional.ofNullable(timeout).orElse(120000);
			RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(timeout.intValue())
					.setSocketTimeout(timeout.intValue()).setConnectTimeout(timeout.intValue()).build();
			httpPost.setConfig(requestConfig);
			CloseableHttpResponse response = closeableHttpClient
					.execute(httpPost);
			HttpEntity httpEntity = response.getEntity();
			if (httpEntity != null) {
				// 打印响应内容
				String retStr = EntityUtils.toString(httpEntity, "UTF-8");
				logger.info("调用响应结果：{}", retStr);
				return formatSoapString(retStr, soapProtocol);
			}
			return "";
		} catch (Exception e) {
			logger.error("调用Webservice接口异常", e);
			t.setStatus(e);
			throw new RuntimeException(e);
		} finally {
			t.complete();
		}
	}

	public static String soapClientWebservice(Pair<String, String> wsdlUrl, String method, Integer timeout, String nameSpaceUri,
			SoapProtocol soapProtocol, Object... params) {
		Transaction t = CatUtils.newTransaction("WebService", wsdlUrl.getKey());
		t.setSuccessStatus();
		try {
			HashMap<String, Object> paramMap = new HashMap<>();
			for (int i = 0; i < params.length; i++) {
				paramMap.put("paramValue" + i, params[i]);
				logger.info("paramValue-{}:{}", i, params[i]);
			}
			timeout = Optional.ofNullable(timeout).orElse(120000);// 默认120秒超时
			SoapClient soapClient = SoapClient.create(wsdlUrl.getKey(), soapProtocol)
					.setMethod(method, nameSpaceUri)
					.setParams(paramMap, false)
					.setConnectionTimeout(timeout.intValue())
					.setReadTimeout(timeout.intValue());
			String sendResult = soapClient.send(true);
			logger.info("sendResult:{}", sendResult);
			return formatSoapString(sendResult, soapProtocol);
		} catch (Exception e) {
			logger.error("soapClientWebservice调用异常,wsdlURL = {};method = {};parameterValues = {}", wsdlUrl.getKey(), method, params);
			t.setStatus(e);
			throw new RuntimeException(e);
		} finally {
			t.complete();
		}
	}

	/**
	 * <p>
	 * Title: formatSoapString
	 * </p>
	 * <p>
	 * Description: 根据soap返回的消息体字符串和soap版本进行字符串转soapMessage消息体
	 * </p>
	 *
	 * @param soapString
	 * @param soapProtocol
	 * @return
	 */
	public static String formatSoapString(String soapString, SoapProtocol soapProtocol) {
		try {
			MessageFactory msgFactory = null;
			if (soapProtocol == SoapProtocol.SOAP_1_1) {
				msgFactory = MessageFactory.newInstance();
			}
			if (soapProtocol == SoapProtocol.SOAP_1_2) {
				msgFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
			}
			SOAPMessage reqMsg = msgFactory.createMessage(new MimeHeaders(), new ByteArrayInputStream(soapString.getBytes(Charset.forName("UTF-8"))));
			// reqMsg.saveChanges();保存修改后的soapmessage
			if (null != reqMsg) {
				Document doc = reqMsg.getSOAPPart().getEnvelope().getBody().extractContentAsDocument();
				// soapBody中的返回信息节点
				Node node = doc.getFirstChild();
				if (node != null) {
					return node.getTextContent().trim();
				} else {
					return "返回值取值失败";
				}
			} else {
				return "返回值取值失败";
			}
		} catch (Exception e) {
			logger.error("解析结果异常", e);
			return null;
		}
	}

	public static String callService(String wsdlUrl, String method, Integer timeout, Object... params) throws ServiceException, RemoteException {
		Transaction t = CatUtils.newTransaction("WebService", wsdlUrl);
		t.setSuccessStatus();
		try {
			logger.info("method:{}", method);

			Service service = new Service();
			Call call = (Call) service.createCall();
			call.setTargetEndpointAddress(wsdlUrl);
			call.setOperationName(method);// 调用的服务接口方法名
			for (int i = 0; i < params.length; i++) {
				call.addParameter("paramName-" + i, XMLType.XSD_ANYTYPE, ParameterMode.IN);// 传递的参数名（可随便取）
				logger.info("paramName-{}:{}", i, params[i]);
			}
			call.setReturnClass(String.class);// 服务方法返回类型
			timeout = Optional.ofNullable(timeout).orElse(120000);// 默认120秒超时
			call.setTimeout(timeout);
			String resultValue = (String) call.invoke(params);
			logger.info("resultValue:{}", resultValue);
			return resultValue;
		} catch (ServiceException e) {
			logger.error("调用Webservice接口异常", e);
			t.setStatus(e);
			throw e;
		} catch (RemoteException e) {
			logger.error("调用Webservice接口异常", e);
			t.setStatus(e);
			throw new RuntimeException(e);
		} finally {
			t.complete();
		}

	}

	/*
	 * 远程访问SOAP协议接口
	 *
	 * @param url： 服务接口地址"http://192.168.0.120:8222/HelloWorld?wsdl"
	 *
	 * @param isClass：接口类名
	 *
	 * @param isMethod： 接口方法名
	 *
	 * @param sendSoapString： soap协议xml格式访问接口
	 *
	 * @return soap协议xml格式
	 *
	 * @备注：有四种请求头格式1、SOAP 1.1； 2、SOAP 1.2 ； 3、HTTP GET； 4、HTTP POST
	 * 参考---》http://www.webxml.com.cn/WebServices/WeatherWebService.asmx?op=
	 * getWeatherbyCityName
	 */
	public static String getWebServiceAndSoap(String url, String isClass, String isMethod, String sendSoapString) throws IOException {
		URL soapUrl = new URL(url);
		URLConnection conn = soapUrl.openConnection();
		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Length",
				Integer.toString(sendSoapString.length()));
		conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		// 调用的接口方法是
		conn.setRequestProperty(isClass, isMethod);
		OutputStream os = conn.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os, "utf-8");
		osw.write(sendSoapString);
		osw.flush();
		osw.close();
		// 获取webserivce返回的流
		InputStream is = conn.getInputStream();
		if (is != null) {
			byte[] bytes = new byte[is.available()];
			is.read(bytes);
			String str = new String(bytes);
			return str;
		} else {
			return null;
		}
	}

	public static void main(String[] args) throws ServiceException, RemoteException {
		String serSupplier = "集中故障系统";
		String serCaller = "运维系统";
		String callerPwd = "口令";
		String callTime = "2022-07-27 17:23:12";
		String alarmId = "BOCO_WNMS_2385421021_966706207_2512061864_3352247650";
		String opDetail = "<opDetail><recordInfo><fieldInfo><fieldChName>网管告警</fieldChName><fieldEnName>alarmId</fieldEnName><fieldContent>%s</fieldContent></fieldInfo><fieldInfo><fieldChName>故障发生时间</fieldChName><fieldEnName>mainFaultGenerantTime</fieldEnName><fieldContent>%s</fieldContent></fieldInfo></recordInfo></opDetail>";
		opDetail = String.format(opDetail, alarmId, "2022-07-27 11:11:11");

		String[] parameterValue = { serSupplier, serCaller, callerPwd, callTime, opDetail };

		String value = callService("http://10.205.196.51:9080/irms.product.alarm.service/services/SheetStateSync?wsdl", "reqAlarmSolveDate", 1000,
				parameterValue);
		System.out.println(value);
	}
}
