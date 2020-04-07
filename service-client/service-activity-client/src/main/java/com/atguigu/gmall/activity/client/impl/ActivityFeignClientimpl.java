package com.atguigu.gmall.activity.client.impl;

import com.atguigu.gmall.activity.client.ActivityFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author 张善涛
 * @create 2020-04-06 13:08
 * @DataType:{ attribute:
 * method:
 * returnType:
 * }
 */
@Component
public class ActivityFeignClientimpl implements ActivityFeignClient{
    @Override
    public Result findAll() {
        return null;
    }

    @Override
    public Result getSeckillGoods(Long skuId) {
        return null;
    }

    @Override
    public Result<Map<String, Object>> trade() {
        return null;
    }


}
