package cn.zwq.filter;

import cn.zwq.cat.context.CatSingleConfig;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CatRunner implements ApplicationRunner {

	@Override
	public void run(ApplicationArguments args) throws Exception {
		CatSingleConfig.setRunning(true);
	}

}
