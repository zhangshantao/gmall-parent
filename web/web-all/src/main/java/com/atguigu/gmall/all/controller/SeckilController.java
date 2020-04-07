package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.activity.client.ActivityFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 张善涛
 * @create 2020-04-06 13:12
 * @DataType:{ attribute:
 * method:
 * returnType:
 * }
 */
@Controller
public class SeckilController {
@Autowired
private ActivityFeignClient activityFeignClient;
    @GetMapping("seckill.html")
    public String seckilllist(Model model){
        Result result = activityFeignClient.findAll();
        model.addAttribute("list",result.getData());
        return "seckill/index";
    }
    @GetMapping("seckill/{skuId}.html")
    public String getseckillById(@PathVariable Long skuId,Model model){
        Result seckillGoods = activityFeignClient.getSeckillGoods(skuId);
        System.err.println(seckillGoods.getData());
        model.addAttribute("item",seckillGoods.getData());
        return "seckill/item";
    }

    @GetMapping("seckill/queue.html")
    public String queue(@RequestParam(name = "skuId") Long skuId,
                        @RequestParam(name = "skuIdStr") String skuIdStr,
                        Model model){
        model.addAttribute("skuId",skuId);
        model.addAttribute("skuIdStr",skuIdStr);
        return "seckill/queue";
    }
}
