package com.atguigu.gmall.oms.listener;

import com.atguigu.gmall.oms.dao.OrderDao;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderListener {
     @Autowired
    private OrderDao orderDao;
    @Autowired
    private AmqpTemplate amqpTemplate;

    @RabbitListener(queues = {"ORDER-DEAD-QUEUE"})
    public void closeOrder(String orderToken){
        //1.关闭订单
        if(this.orderDao.closeOrder(orderToken)==1){
            //2.解锁库存
            this.amqpTemplate.convertAndSend("ORDER-EXCHANGE","stock.unlock",orderToken);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ORDER-PAY-QUEUE",durable = "true"),
            exchange = @Exchange(value = "ORDER-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"order.pay"}
    ))
    public void payOrder(String orderToken){
      //1.更新订单状态,代发货状态
        int flag = this.orderDao.payOrder(orderToken);
        if(flag==1){
            //2.真的减库存操作
            this.amqpTemplate.convertAndSend("ORDER-EXCHANGE","stock.minus",orderToken);
            //3.给用户添加默认值
        }
    }
}
