package cn.zwq.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import cn.zwq.cat.CatProperties;
import cn.zwq.util.SpringBeanUtils;
import com.dianping.cat.Cat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.LogbackException;

public class LogBackCatAppender extends AppenderBase<ILoggingEvent> {
	private CatProperties catProperties;

	private boolean filter(ILoggingEvent event) {
		if (catProperties == null && SpringBeanUtils.hasBean(CatProperties.class)) {
			catProperties = SpringBeanUtils.getBean(CatProperties.class);
		}

		List<String> logFilterPrefixes = Optional.ofNullable(catProperties).map(item -> catProperties.getLogFilterPrefixes())
				.orElse(new ArrayList<>());
		if (logFilterPrefixes.isEmpty()) {
			List<String> packages = Arrays.asList("com.alibaba.nacos", "com.netflix");
			return packages.stream().anyMatch(p -> event.getCallerData()[0].getClassName().startsWith(p));
		}
		return logFilterPrefixes.stream().anyMatch(p -> event.getCallerData()[0].getClassName().startsWith(p));
	}

	@Override
	protected void append(ILoggingEvent event) {
		try {
			Level level = event.getLevel();
			if (level.isGreaterOrEqual(Level.ERROR)) {
				logError(event);
			} else {
				if (filter(event)) {
					return;
				}

				logEvent(event);
			}
		} catch (Exception ex) {
			throw new LogbackException(event.getFormattedMessage(), ex);
		}
	}

	private void logError(ILoggingEvent event) {
		if (Cat.getManager() != null && Cat.getManager().getPeekTransaction() != null) {
			Cat.getManager().getPeekTransaction().setStatus("error");
		}
		ThrowableProxy info = (ThrowableProxy) event.getThrowableProxy();
		if (info != null) {
			Throwable exception = info.getThrowable();

			Object message = event.getFormattedMessage();
			if (message != null) {
				Cat.logError(String.valueOf(message), exception);
			} else {
				Cat.logError(exception);
			}
		}
	}

	private void logEvent(ILoggingEvent event) {
		String type = "Logback";
		String name = event.getLevel().toString();
		Object message = event.getFormattedMessage();
		String data;
		if (message instanceof Throwable) {
			data = buildExceptionStack((Throwable) message);
		} else {
			data = event.getFormattedMessage();
		}

		ThrowableProxy info = (ThrowableProxy) event.getThrowableProxy();
		if (info != null) {
			data = data + '\n' + buildExceptionStack(info.getThrowable());
		}

		Cat.logEvent(type, name, "0", data);
	}

	private String buildExceptionStack(Throwable exception) {
		if (exception != null) {
			StringWriter writer = new StringWriter(2048);
			exception.printStackTrace(new PrintWriter(writer));
			return writer.toString();
		} else {
			return "";
		}
	}

}
