package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author 张善涛
 * @create 2020-04-06 13:00
 * @DataType:{ attribute:
 * method:
 * returnType:
 * }
 */
@Service
public class impl implements SeckillGoodsService {
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public List<SeckillGoods> findAll() {
        List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectList(null);
        return seckillGoods;
    }

    @Override
    public SeckillGoods getSeckillGoods(Long skuId) {
        SeckillGoods seckillGoods = seckillGoodsMapper.selectOne(
                new QueryWrapper<SeckillGoods>().eq("sku_id",skuId));
        return seckillGoods;
    }
    // 预下单  添加订单到缓存
    @Override
    public void YuxiaDan(Long skuId, String userId) {
        // 判断订单是否还有
        String state  = (String) CacheHelper.get(skuId.toString());
        if ("0".equals(state)){
            // 说明库存没了
            System.out.println("判断订单是否还有:没了");
            return;
        }
        // 判断哪用户是否下单了
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_USER + userId, skuId, RedisConst.
                SECKILL__TIMEOUT, TimeUnit.SECONDS);
        if (!aBoolean){
            // 说明没有添加进去，用户已经购买过了
            return;
        }
        // 判断集合中是否还有库存
        String goodId = (String) redisTemplate.boundListOps(
                RedisConst.SECKILL_STOCK_PREFIX + skuId).rightPop();
        if (StringUtils.isEmpty(goodId)){
            redisTemplate.convertAndSend("seckillpush","0");
            return;
        }
        // 添加订单到集合中
        OrderRecode orderRecode = new OrderRecode();
        orderRecode.setNum(1);
        orderRecode.setOrderStr(MD5.encrypt(userId+skuId));
        orderRecode.setUserId(userId);
        orderRecode.setSeckillGoods(getSeckillGoods(skuId));
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).put(orderRecode.getUserId(),orderRecode);
        // 同步库存数据
        updateStockCount(orderRecode.getSeckillGoods().getSkuId());

    }


    // 同步库存数据
    private void updateStockCount(Long skuId) {
        Long size = redisTemplate.boundHashOps(RedisConst.SECKILL_STOCK_PREFIX + skuId).size();
        if (size%2==0){
            // 同步数据到数据库
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.
                    boundHashOps(RedisConst.SECKILL_GOODS).get(skuId.toString());
            seckillGoods.setStockCount(size.intValue());
            seckillGoodsMapper.updateById(seckillGoods);
            // 同步缓存
            redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).put(skuId.toString(),seckillGoods);
        }
    }
    // 检查订单
    @Override
    public Result checkOrder(String userId, Long skuId) {
        // 检查用户订单是否有
        Boolean aBoolean = redisTemplate.hasKey(RedisConst.SECKILL_USER+userId);
        if (aBoolean){
            Boolean flag = redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).hasKey(userId);
            if (flag){
                OrderRecode seckillGoods = (OrderRecode) redisTemplate.
                        boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);
                return Result.build(seckillGoods, ResultCodeEnum.SECKILL_SUCCESS);
            }
        }
        // 用户不能重复下订单
        Boolean aBoolean1 = redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).hasKey(userId);
        if (aBoolean1){
            String orderId = (String) redisTemplate.boundHashOps
                    (RedisConst.SECKILL_ORDERS_USERS).get(userId);
            return Result.build(orderId,ResultCodeEnum.SUCCESS);
        }
        String statue = (String) CacheHelper.get(skuId.toString());
        if ("0".equals(statue)){
            return Result.build(null,ResultCodeEnum.SECKILL_FAIL);
        }

        return Result.build(null,ResultCodeEnum.PAY_RUN);
    }
}
