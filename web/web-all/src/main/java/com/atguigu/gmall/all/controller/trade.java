package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.activity.client.ActivityFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * @author 张善涛
 * @create 2020-04-06 23:40
 * @DataType:{ attribute:
 * method:
 * returnType:
 * }
 */
@Controller
public class trade {
    @Autowired
    private ActivityFeignClient activityFeignClient;
  @GetMapping("seckill/trade.html")
    public String trade(Model model){
      Result<Map<String,Object>> trade1 = activityFeignClient.trade();
      if (trade1.isOk()){
          model.addAllAttributes(trade1.getData());
          return "seckill/trade";
      }
      else {
          return "seckill/fail";
      }
  }
}
