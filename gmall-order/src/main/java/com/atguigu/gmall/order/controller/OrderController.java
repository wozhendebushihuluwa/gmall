package com.atguigu.gmall.order.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderComfirmVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;



    @GetMapping("confirm")
    public Resp<OrderComfirmVO> confirm(){
        OrderComfirmVO orderComfirmVO= this.orderService.confirm();
        return Resp.ok(orderComfirmVO);
    }

    @PostMapping("submit")
    public Resp<Object> submit(@RequestBody OrderSubmitVO orderSubmitVO){
        this.orderService.submit(orderSubmitVO);
        return Resp.ok(null);
    }
}
