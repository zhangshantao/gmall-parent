package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.SeckillGoodsService;

import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.message.ReusableMessage;
import org.redisson.misc.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import sun.plugin.cache.CacheUpdateHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author 张善涛
 * @create 2020-04-06 12:58
 * @DataType:{ attribute:
 * method:
 * returnType:
 * }
 */
@RestController
@RequestMapping("/api/activity/seckill")
public class SeckillGoodsController {
    @Autowired
    private SeckillGoodsService seckillGoodsService;
    @Autowired
    private RabbitService rabbitService;
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private RedisTemplate redisTemplate;
   @GetMapping("/findAll")
    public Result findAll(){
      List<SeckillGoods> seckillGoodsList= seckillGoodsService.findAll();
       return Result.ok(seckillGoodsList);
   }
    @GetMapping("/getSeckillGoods/{skuId}")
    public Result getSeckillGoods(@PathVariable Long skuId){
       SeckillGoods seckillGoods= seckillGoodsService.getSeckillGoods(skuId);
       return Result.ok(seckillGoods);
    }
    // 下单马
    @GetMapping("auth/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuIdStr(@PathVariable("skuId") Long skuId, HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        SeckillGoods seckillGoods = seckillGoodsService.getSeckillGoods(skuId);
        if (seckillGoods!=null){
            Date date = new Date();
            if (DateUtil.dateCompare(seckillGoods.getStartTime(),date)
                    &&DateUtil.dateCompare(date,seckillGoods.getEndTime())){
                String encrypt = MD5.encrypt(userId);
                return Result.ok(encrypt);
            }
        }
        return Result.ok().message("西单失败");
    }
    // 预下单
    @PostMapping("auth/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable Long skuId,HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        String skuIdStr = request.getParameter("skuIdStr");
        if (!skuIdStr.equals(MD5.encrypt(userId))){
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }
        String statu = (String) CacheHelper.get(skuId.toString());
        if (StringUtils.isEmpty(statu)){
            return Result.build(null,ResultCodeEnum.SECKILL_ILLEGAL);
        }
        if ("1".equals(statu)){
            UserRecode userRecode = new UserRecode();
            userRecode.setUserId(userId);
            userRecode.setSkuId(skuId);
            // 用seckill监听
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SECKILL_USER,MqConst.ROUTING_SECKILL_USER,
                    userRecode);
        }
        else {
            return Result.build(null,ResultCodeEnum.SECKILL_FINISH);
        }
       return Result.ok();
    }
    // 检查订单
    @GetMapping("auth/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable Long skuId,HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        return seckillGoodsService.checkOrder(userId,skuId);
    }
    // 下订单
    @GetMapping("auth/trade")
    public Result<Map<String,Object>> trade(HttpServletRequest request){
       // 获取地址
        String userId = AuthContextHolder.getUserId(request);
        List<UserAddress> userAddressListByUserId =
                userFeignClient.findUserAddressListByUserId(userId);
        // 获取订单集合
        OrderRecode orderRecode = (OrderRecode) redisTemplate.boundHashOps
                (RedisConst.SECKILL_ORDERS).get(userId);
        SeckillGoods seckillGoods = orderRecode.getSeckillGoods();
        ArrayList<OrderDetail> orderDetails = new ArrayList<>();
        if (orderRecode==null)return Result.fail();
        if (seckillGoods!=null){
            OrderDetail detail = new OrderDetail();
            detail.setImgUrl(seckillGoods.getSkuDefaultImg());
            detail.setOrderPrice(seckillGoods.getCostPrice());
            detail.setSkuId(seckillGoods.getSkuId());
           // detail.setSkuName(seckillGoods.getSkuName());
            detail.setSkuName(seckillGoods.getSkuName());
           // detail.setSkuNum(seckillGoods.getNum());
            detail.setSkuNum(orderRecode.getNum());
            orderDetails.add(detail);
        }
        // 获取总价格
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetails);
        orderInfo.sumTotalAmount();
        // 返回数据
        Map<String, Object> map = new HashMap<>();
        map.put("userAddressList",userAddressListByUserId);
        map.put("detailArrayList",orderDetails);
        map.put("totalAmount",orderInfo.getTotalAmount());
        return Result.ok(map);
    }
    @Autowired
    private OrderFeignClient orderFeignClient;
    @PostMapping("auth/submitOrder")
    public Result TijianDingDan(@RequestBody OrderInfo orderInfo,HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        OrderRecode orderRecode = (OrderRecode) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);
        if (orderRecode!=null) {
            SeckillGoods seckillGoods = orderRecode.getSeckillGoods();
            Long orderId= orderFeignClient.sub(orderInfo);
            if (orderId==null){
                return Result.fail().message("联系妹子");
            }
            else {
                redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).delete(userId);
                redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).put(userId,orderId.toString());
                return Result.ok(orderId);
            }
        }else {
            return Result.fail().message("下单失败");
        }

    }
}
