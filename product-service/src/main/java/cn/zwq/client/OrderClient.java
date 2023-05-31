package cn.zwq.client;

import cn.zwq.api.OrderApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author zhangwenqia
 * @create 2023-05-17 17:02
 * @description 类描述
 */
@FeignClient(value = "order-service")
public interface OrderClient extends OrderApi {
}
