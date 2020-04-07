package com.atguigu.gmall.order.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.stereotype.Component;
import com.atguigu.gmall.order.client.OrderFeignClient;

import java.util.Map;

/**
 * @author 张善涛
 * @create 2020-04-07 13:24
 * @DataType:{ attribute:
 * method:
 * returnType:
 * }
 */
@Component
public class OrderFeignClientimpl implements OrderFeignClient {

    @Override
    public Result<Map<String, Object>> trade() {
        return null;
    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        return null;
    }

    @Override
    public Long sub(OrderInfo orderInfo) {
        return null;
    }
}
