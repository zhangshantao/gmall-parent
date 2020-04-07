package com.atguigu.gmall.order.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.impl.OrderFeignClientimpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.atguigu.gmall.order.client.impl.OrderFeignClientimpl;

import java.util.Map;

/**
 * @author mqx
 * @date 2020/3/28 14:50
 */
@FeignClient(name = "service-order",fallback = OrderFeignClientimpl.class)
public interface OrderFeignClient {

    // 远程调用接口
    // 通过feign调用的时候，我们有个拦截器它会自动将userId 获取到了。
    @GetMapping("/api/order/auth/trade")
    Result<Map<String,Object>> trade();

    @GetMapping("/api/order/inner/getOrderInfo/{orderId}")
    OrderInfo getOrderInfo(@PathVariable Long orderId);

    @PostMapping("/api/order/inner/seckill/sub")
    Long sub(@RequestBody OrderInfo orderInfo);
}
