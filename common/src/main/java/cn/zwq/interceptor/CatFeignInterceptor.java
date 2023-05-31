package cn.zwq.interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cn.zwq.cat.CatProperties;
import cn.zwq.cat.context.CatContext;
import cn.zwq.cat.util.CatServiceLogUtils;
import cn.zwq.cat.util.CatUtils;
import cn.zwq.util.SpringBeanUtils;
import cn.zwq.util.ThreadLocalUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

@Service
@ConditionalOnProperty(value = "cat.config.enabled", havingValue = "true")
public class CatFeignInterceptor implements RequestInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(CatFeignInterceptor.class);

	private CatProperties catProperties;

	@Autowired
	public void setCatProperties(CatProperties catProperties) {
		this.catProperties = catProperties;
	}

	@Override
	public void apply(RequestTemplate template) {
		String catName = template.path();
		if (catProperties == null) {
			catProperties = SpringBeanUtils.getBean(CatProperties.class);
		}
		List<String> allInOnePaths = Optional.ofNullable(catProperties).map(value -> value.getAllInOnePaths()).orElseGet(() -> new ArrayList<>());
		catName = CatUtils.getPathName(catName, allInOnePaths);

		Transaction t = CatUtils.getPigeonCallTransaction(catProperties, catName);

		try {
			ThreadLocalUtil.put(CatUtils.KEY_TRANSACTION, t);
			logger.info("Feign-url:{}", template.url());

			CatContext context = new CatContext();
			String domain = Optional.ofNullable(catProperties).map(value -> value.getDomain()).orElse("zwq");
			Cat.logRemoteCallClient(context, domain); // 声明子节点
			String catId = context.getProperty(Cat.Context.CHILD);
			String parentId = context.getProperty(Cat.Context.PARENT);
			String catRootId = context.getProperty(Cat.Context.ROOT);
			template.header(CatServiceLogUtils.KEY_CHILD, catId);
			template.header(CatServiceLogUtils.KEY_PARENT, parentId);
			template.header(CatServiceLogUtils.KEY_ROOT, catRootId);
			// template.header(CatServiceLogUtils.KEY_TRACE_MODE, "true");
			template.header(CatServiceLogUtils.KEY_CLIENT_SYSTEM, CatServiceLogUtils.getClientSystem());
		} catch (Exception e) {
			CatUtils.setStatus(t, e);
			CatUtils.closeTransaction(t);
			ThreadLocalUtil.remove(CatUtils.KEY_REST_TRANSACTION);
			throw e;
		}
	}
}
