package com.atguigu.gmall.activity.client;

import com.atguigu.gmall.activity.client.impl.ActivityFeignClientimpl;
import com.atguigu.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author 张善涛
 * @create 2020-04-06 13:07
 * @DataType:{ attribute:
 * method:
 * returnType:
 * }
 */
@FeignClient(value = "service-activity",fallback = ActivityFeignClientimpl.class)
public interface ActivityFeignClient {
    @GetMapping("/api/activity/seckill/findAll")
    public Result findAll();
    @GetMapping("/api/activity/seckill/getSeckillGoods/{skuId}")
    public Result getSeckillGoods(@PathVariable Long skuId);
    // 下订单
    @GetMapping("/api/activity/seckill/auth/trade")
    public Result<Map<String,Object>> trade();
}
