package com.atguigu.gmall.activity.receiver;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author 张善涛
 * @create 2020-04-06 16:07
 * @DataType:{ attribute:
 * method:
 * returnType:
 * }
 */
@Component
public class seckill {
  @Autowired
  private SeckillGoodsService seckillGoodsService;
  @Autowired
  private SeckillGoodsMapper seckillGoodsMapper;
  @Autowired
  private RedisTemplate redisTemplate;
  @SneakyThrows
  @RabbitListener(bindings = @QueueBinding(value = @Queue(MqConst.QUEUE_SECKILL_USER),
  exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_SECKILL_USER),key =
          {MqConst.ROUTING_SECKILL_USER}))
  public void skill(UserRecode userRecode, Message message, Channel channel){
            // 预下单  添加订单到缓存
    seckillGoodsService.YuxiaDan(userRecode.getSkuId(),userRecode.getUserId());
    channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
  }
  @SneakyThrows
  @RabbitListener(bindings = @QueueBinding(value = @Queue(value = MqConst.QUEUE_TASK_18),
  exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
  key = {MqConst.ROUTING_TASK_18}))
  public void seckill(Message message,Channel channel){
    // 删除缓存
       // 1.需要知道删除那些商品的库存
    QueryWrapper<SeckillGoods> wrapper = new QueryWrapper<>();
    wrapper.eq("status","1");
    wrapper.le("end_time",new Date());
    List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectList(wrapper);
    for (SeckillGoods seckillGood : seckillGoods) {
         redisTemplate.delete(RedisConst.SECKILL_STOCK_PREFIX+seckillGood.getSkuId());
      seckillGood.setStatus("2");
      seckillGoodsMapper.updateById(seckillGood);
    }
    redisTemplate.delete(RedisConst.SECKILL_GOODS);
    redisTemplate.delete(RedisConst.SECKILL_ORDERS);
    redisTemplate.delete(RedisConst.SECKILL_ORDERS_USERS);
    //2.删除商品，订单，已经支付了的订单
    channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
  }
}
