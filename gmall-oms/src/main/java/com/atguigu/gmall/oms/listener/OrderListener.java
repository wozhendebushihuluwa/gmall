package com.atguigu.gmall.oms.listener;

import com.atguigu.gmall.oms.dao.OrderDao;
import org.springframework.amqp.core.AmqpTemplate;
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
}
