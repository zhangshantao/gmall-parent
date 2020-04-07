package com.atguigu.gmall.activity.receiver;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author 张善涛
 * @create 2020-04-06 9:59
 * @DataType:{ attribute:
 * method:
 * returnType:
 * }
 */
@Component
public class SeckillReceiver {
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = MqConst.QUEUE_TASK_1),
    exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
    key = {MqConst.ROUTING_TASK_1}))
    public void importItemToRedis(Message message, Channel channel) throws IOException {
        QueryWrapper<SeckillGoods> seckillGoodsQueryWrapper = new QueryWrapper<>();
        seckillGoodsQueryWrapper.eq("status",1).gt("stock_count",0).eq("" +
                "DATE_FORMAT(start_time,'%Y-%m-%d')", DateUtil.formatDate(new Date()));
        List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectList(seckillGoodsQueryWrapper);
        if (seckillGoods!=null&&seckillGoods.size()>0){
            for (SeckillGoods seckillGood : seckillGoods) {
                Boolean aBoolean = redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).
                        hasKey(seckillGood.getId().toString());
                if (aBoolean){
                    continue;
                }
                redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).put(seckillGood.getSkuId().toString(),
                        seckillGood);
                for (Integer i = 0; i < seckillGood.getStockCount(); i++) {
                    redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX+seckillGood.getSkuId().toString()).
                            leftPush(seckillGood.getId().toString());
                }
               redisTemplate.convertAndSend("seckillpush",seckillGood.getSkuId()+":1");
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
    }

}
