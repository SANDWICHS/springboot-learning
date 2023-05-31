package cn.zwq.http;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.wsdl.Binding;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.encoding.Deserializer;
import javax.xml.rpc.encoding.DeserializerFactory;

import cn.zwq.cat.context.CatContext;
import cn.zwq.cat.util.CatServiceLogUtils;
import org.apache.axis.Constants;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.encoding.ser.SimpleDeserializer;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.utils.StringUtils;
import org.apache.axis.wsdl.gen.Parser;
import org.apache.axis.wsdl.symbolTable.*;
import org.slf4j.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

import sun.reflect.generics.tree.BaseType;

/**
 * @author zhangwenqia
 * @create 2022-06-02 14:12
 * @description 类描述
 */
public class WebServiceInvokerUtil {
	static Logger logger = org.slf4j.LoggerFactory.getLogger(WebServiceInvokerUtil.class);

	static Map<String, WsdlModel> serviceMap = new HashMap<>();

	private static synchronized WsdlModel init(String wsdlUrl) throws Exception {
		if (null == serviceMap.get(wsdlUrl)) {
			WsdlModel wsdlModel = new WsdlModel();
			wsdlModel.setWsdlParser(new Parser());
			wsdlModel.getWsdlParser().run(wsdlUrl);
			Map services = enumSymTabEntry(wsdlModel, ServiceEntry.class);
			wsdlModel.setServices(services);
			String serviceName = enumServiceNames(services);
			wsdlModel.setServiceName(serviceName);
			String portName = enumPortNames(services, serviceName);
			wsdlModel.setPortName(portName);
			ServiceEntry serviceEntry = (ServiceEntry) services.get(serviceName);
			wsdlModel.setServiceEntry(serviceEntry);
			Service service = serviceEntry.getService();
			wsdlModel.setService(service);
			org.apache.axis.client.Service clientService = new org.apache.axis.client.Service(wsdlModel.getWsdlParser(), service.getQName());
			wsdlModel.setClientService(clientService);
			serviceMap.put(wsdlUrl, wsdlModel);
			return wsdlModel;
		} else {
			return serviceMap.get(wsdlUrl);
		}

	}

	// 普通webService的调用
	public static Object invoke(org.unidal.tuple.Pair<String, String> wsdlUrlPair, Integer timeout, String operationName, Object... parameterValues) {
		Transaction t = Cat.newTransaction("WebService", wsdlUrlPair.getKey());
		/*
		 * CatContext context = new CatContext(); Cat.logRemoteCallClient(context); //
		 * 声明子节点
		 */
		String returnName = null;
		WsdlModel wsdlModel;
		try {
			logger.info("开始动态调用:{}" + wsdlUrlPair.getKey());
			long begin = System.currentTimeMillis();
			Vector<Object> inputs = new Vector<>();
			if (null == serviceMap.get(wsdlUrlPair.getKey())) {
				wsdlModel = init(wsdlUrlPair.getKey());
			} else {
				wsdlModel = serviceMap.get(wsdlUrlPair.getKey());
			}

			Call call = wsdlModel.getClientService().createCall(QName.valueOf(wsdlModel.getPortName()), QName.valueOf(operationName));
			timeout = Optional.ofNullable(timeout).orElse(120000);// 默认120秒超时
			((org.apache.axis.client.Call) call).setTimeout(timeout);
			logger.info("创建Call成功");
			// setHeaders(call, context);
			// setHeaders(call, parameterValues);

			BindingEntry bindingEntry = getBindingEntry(wsdlModel);
			Operation operation = getOperation(bindingEntry, operationName);
			Parameters parameters = bindingEntry.getParameters(operation);
			logger.info("获取调用参数信息成功");
			if (parameters.returnParam != null) {
				QName returnType = org.apache.axis.wsdl.toJava.Utils.getXSIType(parameters.returnParam);
				returnName = parameters.returnParam.getQName().getLocalPart();
			}

			int size = parameters.list.size();
			StringBuffer paramsSb = new StringBuffer();
			paramsSb.append("operationName:" + operationName).append(System.lineSeparator());
			paramsSb.append("---->parametersSize:" + size).append(System.lineSeparator());
			paramsSb.append("=====>value:" + parameterValues.length).append(System.lineSeparator());
			paramsSb.append("class" + parameterValues.getClass());

			for (int i = 0; i < size; i++) {
				Parameter p = (Parameter) parameters.list.get(i);
				paramsSb.append("=======>p:" + p.getQName().getLocalPart()).append(System.lineSeparator());
				switch (p.getMode()) {
				case Parameter.IN:
					inputs.add(getParamData((org.apache.axis.client.Call) call, p, parameterValues[i]));
					paramsSb.append("=======>parameterValue:" + parameterValues[i]).append(System.lineSeparator());
					break;
				case Parameter.OUT:
					break;
				case Parameter.INOUT:
					inputs.add(getParamData((org.apache.axis.client.Call) call, p, String.valueOf(parameterValues[i])));
					break;
				}
			}

			paramsSb.append("==>inputs:" + inputs.size());
			logger.info(paramsSb.toString());
			Object[] parAry = new Object[inputs.size()];
			for (int i = 0; i < inputs.size(); i++) {
				parAry[i] = inputs.get(i);
			}

			// 不加这句会导致通过gateway的webservice外部无法调用
			if (!StringUtils.isEmpty(wsdlUrlPair.getValue())) {
				logger.info("开始设置setTargetEndpointAddress：{}", wsdlUrlPair.getValue());
				((org.apache.axis.client.Call) call).setTargetEndpointAddress(new URL(wsdlUrlPair.getValue()));
			}

			logger.info("输出targetEndpointAddress：{}", call.getTargetEndpointAddress());
			logger.info("输出call：{}", call);
			Object ret = call.invoke(parAry);

			Map outputs = call.getOutputParams();

			Map<String, Object> map = new HashMap<>();
			if (ret != null && returnName != null) {
				map.put(returnName, ret);
			}
			if (outputs != null) {
				Iterator outIterator = outputs.keySet().iterator();
				while (outIterator.hasNext()) {
					Object obj = outIterator.next();
					String name;
					if (obj.getClass().getName().equals("java.lang.String")) {
						name = (String) obj;
					} else {
						name = ((QName) obj).getLocalPart();
					}
					map.put(name, outputs.get(obj));
				}
			}
			long end = System.currentTimeMillis();

			logger.info("{}调用返回值：{} 调用花费时间：{}", wsdlUrlPair.getKey(), map.get(returnName), (end - begin));
			t.setSuccessStatus();
			return map.get(returnName);
		} catch (Exception e) {
			serviceMap.remove(wsdlUrlPair.getKey());
			// Cat.getProducer().logError(e);
			t.setStatus(e);
			throw new RuntimeException(e);
		} finally {
			t.complete();
		}
	}

	private static void setHeaders(Call call, CatContext context) {
		String targetEndPointAddress = "";
		if (null != targetEndPointAddress && !"".equals(targetEndPointAddress)) {
			SOAPHeaderElement el1 = new SOAPHeaderElement(new QName(CatServiceLogUtils.KEY_ROOT), context.getProperty(Cat.Context.ROOT));
			SOAPHeaderElement el2 = new SOAPHeaderElement(new QName(CatServiceLogUtils.KEY_PARENT), context.getProperty(Cat.Context.PARENT));
			SOAPHeaderElement el3 = new SOAPHeaderElement(new QName(CatServiceLogUtils.KEY_CHILD), context.getProperty(Cat.Context.CHILD));
			// SOAPHeaderElement el4 = new SOAPHeaderElement(new
			// QName(CatServiceLogUtils.KEY_TRACE_MODE), "true");
			SOAPHeaderElement el5 = new SOAPHeaderElement(new QName(CatServiceLogUtils.KEY_CLIENT_SYSTEM), CatServiceLogUtils.getClientSystem());
			((org.apache.axis.client.Call) call).addHeader(el1);
			((org.apache.axis.client.Call) call).addHeader(el2);
			((org.apache.axis.client.Call) call).addHeader(el3);
			// ((org.apache.axis.client.Call) call).addHeader(el4);
			((org.apache.axis.client.Call) call).addHeader(el5);
			call.setTargetEndpointAddress(targetEndPointAddress);
		}
	}

	private static void setHeaders(Call call, Object[] parameterValues) {
		// ( (org.apache.axis.client.Call) call).setUsername("alarm");
		// ( (org.apache.axis.client.Call) call).setPassword("12345678");
		// 上海需求,先调用ESB,再通过ESB调用EOMS----add by zkc
		String targetEndPointAddress = "";
		if (null != targetEndPointAddress && !"".equals(targetEndPointAddress)) {
			String serviceCode = String.valueOf(parameterValues[parameterValues.length - 4]);
			String userName = String.valueOf(parameterValues[parameterValues.length - 3]);
			String authCode = String.valueOf(parameterValues[parameterValues.length - 2]);
			String eventId = String.valueOf(parameterValues[parameterValues.length - 1]);
			SOAPHeaderElement el1 = new SOAPHeaderElement(new QName("cn.com.boco.HermesService", "ServiceCode"), serviceCode);
			SOAPHeaderElement el2 = new SOAPHeaderElement(new QName("cn.com.boco.HermesService", "UserName"), userName);
			SOAPHeaderElement el3 = new SOAPHeaderElement(new QName("cn.com.boco.HermesService", "AuthCode"), authCode);
			SOAPHeaderElement el4 = new SOAPHeaderElement(new QName("cn.com.boco.HermesService", "EventId"), eventId);
			((org.apache.axis.client.Call) call).addHeader(el1);
			((org.apache.axis.client.Call) call).addHeader(el2);
			((org.apache.axis.client.Call) call).addHeader(el3);
			((org.apache.axis.client.Call) call).addHeader(el4);
			call.setTargetEndpointAddress(targetEndPointAddress);
		}
	}

	private static String enumServiceNames(Map services) {
		Vector vector = new Vector();
		Iterator iterator = services.keySet().iterator();
		while (iterator.hasNext()) {
			String s = (String) iterator.next();
			vector.addElement(s);
		}
		return (String) vector.get(0);
	}

	private static String enumPortNames(Map services, String serviceName) {
		Vector vector = new Vector();
		ServiceEntry serviceEntry = (ServiceEntry) services.get(serviceName);
		Map ports = serviceEntry.getService().getPorts();
		Iterator i = ports.keySet().iterator();
		while (i.hasNext()) {
			String s = (String) i.next();
			vector.addElement(s);
		}
		return (String) vector.get(0);
	}

	/*
	 * public Vector enumOperationNames(String serviceName, String portName){ Vector
	 * v = new Vector(); BindingEntry entry = getBindingEntry(serviceName,
	 * portName); Set operations = entry.getOperations(); Iterator i =
	 * operations.iterator(); while (i.hasNext()){ Operation o = (Operation)
	 * i.next(); String s = o.getName(); v.addElement(s); } return v; }
	 *
	 * public Parameters enumParameters(String serviceName, String portName, String
	 * operationName){ BindingEntry entry = getBindingEntry(serviceName, portName);
	 * Operation o = getOperation(entry, operationName); Parameters parameters =
	 * entry.getParameters(o); return parameters; }
	 *
	 * public String getParameterModeString(Parameter p){ String ret = null; switch
	 * (p.getMode()){ case Parameter.IN: ret = "[IN]"; break; case Parameter.INOUT:
	 * ret = "[IN, OUT]"; break; case Parameter.OUT: ret = "[OUT]"; break; } return
	 * ret; }
	 */

	private static BindingEntry getBindingEntry(WsdlModel wsdlModel) {
		ServiceEntry serviceEntry = (ServiceEntry) wsdlModel.getServices().get(wsdlModel.getServiceName());
		Port port = serviceEntry.getService().getPort(wsdlModel.getPortName());
		Binding binding = port.getBinding();
		SymbolTable table = wsdlModel.getWsdlParser().getSymbolTable();
		return table.getBindingEntry(binding.getQName());
	}

	private static Operation getOperation(BindingEntry entry, String operationName) {
		Iterator operations = entry.getOperations().iterator();
		while (operations.hasNext()) {
			Operation o = (Operation) operations.next();
			if (operationName.equals(o.getName())) {
				return o;
			}
		}
		return null;
	}

	private static Map<String, SymTabEntry> enumSymTabEntry(WsdlModel wsdlModel, Class cls) {
		// return Map of <QName.getLocalPart, SymTabEntry>
		Map<String, SymTabEntry> ret = new ConcurrentHashMap();
		HashMap map = wsdlModel.getWsdlParser().getSymbolTable().getHashMap();
		Iterator iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			QName key = (QName) entry.getKey();
			Vector vector = (Vector) entry.getValue();
			int size = vector.size();
			for (int i = 0; i < size; ++i) {
				SymTabEntry symTabEntry = (SymTabEntry) vector.elementAt(i);
				if (cls.isInstance(symTabEntry)) {
					ret.put(key.getLocalPart(), symTabEntry);
				}
			}
		}
		return ret;
	}

	private static Object getParamData(org.apache.axis.client.Call call, Parameter parameter, Object arg) throws Exception {
		// Get the QName representing the parameter type
		QName paramType = org.apache.axis.wsdl.toJava.Utils.getXSIType(parameter);
		TypeEntry type = parameter.getType();
		if (type instanceof BaseType && type.isBaseType()) {
			DeserializerFactory factory = call.getTypeMapping().getDeserializer(paramType);
			Deserializer deserializer = factory.getDeserializerAs(Constants.AXIS_SAX);
			if (deserializer instanceof SimpleDeserializer) {
				return ((SimpleDeserializer) deserializer).makeValue(String.valueOf(arg));
			}
		} else {
			QName qn = QName.valueOf("");
			call.registerTypeMapping(arg.getClass(), qn,
					new BeanSerializerFactory(arg.getClass(), qn),
					new BeanDeserializerFactory(arg.getClass(), qn));
			return arg;
		}
		return null;
	}

	public static class WsdlModel {
		private Parser wsdlParser;
		private String serviceName;
		private String portName;
		private Map services;
		private ServiceEntry serviceEntry;
		private Service service;
		private org.apache.axis.client.Service clientService;

		public void setWsdlParser(Parser wsdlParser) {
			this.wsdlParser = wsdlParser;
		}

		public Parser getWsdlParser() {
			return wsdlParser;
		}

		public void setServiceName(String serviceName) {
			this.serviceName = serviceName;
		}

		public String getServiceName() {
			return serviceName;
		}

		public void setPortName(String portName) {
			this.portName = portName;
		}

		public String getPortName() {
			return portName;
		}

		public void setServices(Map services) {
			this.services = services;
		}

		public Map getServices() {
			return services;
		}

		public void setServiceEntry(ServiceEntry serviceEntry) {
			this.serviceEntry = serviceEntry;
		}

		public ServiceEntry getServiceEntry() {
			return serviceEntry;
		}

		public void setService(Service service) {
			this.service = service;
		}

		public Service getService() {
			return service;
		}

		public void setClientService(org.apache.axis.client.Service clientService) {
			this.clientService = clientService;
		}

		public org.apache.axis.client.Service getClientService() {
			return clientService;
		}
	}

}
