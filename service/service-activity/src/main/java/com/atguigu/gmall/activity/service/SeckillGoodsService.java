package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;

import java.util.List;

/**
 * @author 张善涛
 * @create 2020-04-06 12:59
 * @DataType:{ attribute:
 * method:
 * returnType:
 * }
 */
public interface SeckillGoodsService {
    // 查询所有的秒杀商品
    List<SeckillGoods> findAll();
    // 根据Id查询秒杀商品
    SeckillGoods getSeckillGoods(Long skuId);

    // 预下单  添加订单到缓存
    void YuxiaDan(Long skuId, String userId);
    // 检查订单
    Result checkOrder(String userId, Long skuId);
}
