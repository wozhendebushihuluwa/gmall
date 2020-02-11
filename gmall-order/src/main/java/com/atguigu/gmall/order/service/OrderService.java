package com.atguigu.gmall.order.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.core.exception.OrderException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderItemVO;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderComfirmVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallSmsClient gmallSmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private GmallUmsClient gmallUmsClient;
    @Autowired
    private GmallCartClient gmallCartClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private GmallOmsClient gmallOmsClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final String TOKEN_PREFIX="order:token:";

    public OrderComfirmVO confirm() {
        OrderComfirmVO orderComfirmVO = new OrderComfirmVO();

        //获取用户的登录信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //获得用户地址信息（远程连接）
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            Resp<List<MemberReceiveAddressEntity>> userResp = this.gmallUmsClient.queryAddressesByUserId(userInfo.getUserId());
            List<MemberReceiveAddressEntity> addressEntities = userResp.getData();
            orderComfirmVO.setAddresses(addressEntities);
        }, threadPoolExecutor);
        //获取订单详情列表（远程接口）
        //远程调用购物车的接口获取选中的购物车记录
        CompletableFuture<Void> orderFuture = CompletableFuture.supplyAsync(() -> {
            return this.gmallCartClient.queryCheckedCarts(userInfo.getUserId());
        }).thenAcceptAsync(carts -> {
            List<OrderItemVO> orderItems = carts.stream().map(cart -> {
                Long skuId = cart.getSkuId();
                Integer count = cart.getCount();
                OrderItemVO orderItemVO = new OrderItemVO();

                orderItemVO.setCount(count);
                orderItemVO.setSkuId(skuId);
                //查询sku相关信息
                CompletableFuture<Void> skuFuture = CompletableFuture.runAsync(() -> {
                    Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(skuId);
                    SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
                    if (skuInfoEntity != null) {
                        orderItemVO.setPrice(skuInfoEntity.getPrice());
                        orderItemVO.setImage(skuInfoEntity.getSkuDefaultImg());
                        orderItemVO.setSkuTitle(skuInfoEntity.getSkuTitle());
                        orderItemVO.setWeight(skuInfoEntity.getWeight());

                    }
                }, threadPoolExecutor);
                //查询商品库存信息
                CompletableFuture<Void> wareFuture = CompletableFuture.runAsync(() -> {
                    Resp<List<WareSkuEntity>> queryWareEntityResp = this.gmallWmsClient.queryWareSkuBySkuId(skuId);
                    List<WareSkuEntity> wareSkuEntities = queryWareEntityResp.getData();
                    if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                        orderItemVO.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
                    }
                }, threadPoolExecutor);
                //查询销售属性
                CompletableFuture<Void> saleAttrValueFuture = CompletableFuture.runAsync(() -> {
                    Resp<List<SkuSaleAttrValueEntity>> saleAttrEntityResp = this.gmallPmsClient.querySaleAttrValueBySkuId(skuId);
                    List<SkuSaleAttrValueEntity> saleAttrValueEntities = saleAttrEntityResp.getData();
                    if (!CollectionUtils.isEmpty(saleAttrValueEntities)) {
                        orderItemVO.setSaleAttrs(saleAttrValueEntities);
                    }
                }, threadPoolExecutor);
                //查询营销信息
                CompletableFuture<Void> saleFuture = CompletableFuture.runAsync(() -> {
                    Resp<List<ItemSaleVo>> saleResp = this.gmallSmsClient.queryItemSaleVoBySkuid(skuId);
                    List<ItemSaleVo> itemSaleVos = saleResp.getData();
                    orderItemVO.setSales(itemSaleVos);
                }, threadPoolExecutor);

                CompletableFuture.allOf(skuFuture, wareFuture, saleAttrValueFuture, saleFuture).join();

                return orderItemVO;
            }).collect(Collectors.toList());
            orderComfirmVO.setOrderItems(orderItems);
        }, threadPoolExecutor);

        //获取用户积分信息ums
        CompletableFuture<Void> boundsFuture = CompletableFuture.runAsync(() -> {
            Resp<MemberEntity> memberEntityResp = this.gmallUmsClient.queryMemberById(userInfo.getUserId());
            MemberEntity memberEntity = memberEntityResp.getData();
            if (memberEntity != null) {
                orderComfirmVO.setBounds(memberEntity.getIntegration());
            }
        }, threadPoolExecutor);

        //防止重复提交的唯一标志
        //uuid可读性很差
        //redis incr id长度不一致
        //分布式id生成器（mybatis-plus提供）
        CompletableFuture<Void> idFuture = CompletableFuture.runAsync(() -> {
            String orderToken = IdWorker.getTimeId();
            orderComfirmVO.setOrderToken(orderToken);//浏览器保存一份
            this.redisTemplate.opsForValue().set(TOKEN_PREFIX + orderToken, orderToken, 3, TimeUnit.HOURS);//保存redis一份
        }, threadPoolExecutor);

        CompletableFuture.allOf(addressFuture,orderFuture,boundsFuture,idFuture).join();

        return orderComfirmVO;
    }

    public OrderEntity submit(OrderSubmitVO orderSubmitVO) {

        //1.检验是否重复提交（是：提示；否：跳转到支付页面，创建订单）
        //判断redis中有没有，有-说明第一次提交，放行并删除redis中的orderToken
        String orderToken = orderSubmitVO.getOrderToken();
//        String token = this.redisTemplate.opsForValue().get(TOKEN_PREFIX + orderSubmitVO.getOrderToken());
//        if(StringUtils.isEmpty(token)){
//          return;
//        }
//        this.redisTemplate.delete(TOKEN_PREFIX+orderSubmitVO.getOrderToken());
        String script="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long flag = (Long)this.redisTemplate.execute(new DefaultRedisScript<>(script,Long.class), Arrays.asList(TOKEN_PREFIX + orderSubmitVO.getOrderToken()), orderToken);
        if(flag ==0){
          throw new OrderException("请不要重复提交订单");
        }
        //2.验价（总价格是否发生了变化）
        BigDecimal totalPrice = orderSubmitVO.getTotalPrice(); //获取页面提交的总价格
        //获取数据库的实时价格
        List<OrderItemVO> items = orderSubmitVO.getItems();
        if(CollectionUtils.isEmpty(items)){
         throw new OrderException("请勾选要购买的商品");
        }
        BigDecimal currentTotalPrice = items.stream().map(orderItemVO -> {
            Long skuId = orderItemVO.getSkuId();
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(skuId);
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if(skuInfoEntity!=null){
                return skuInfoEntity.getPrice().multiply(new BigDecimal(orderItemVO.getCount()));//获取每个sku的实时价格 *count
            }
            return new BigDecimal(0);
        }).reduce((a,b)-> a.add(b)).get();
        //比较价格是否一致
        if(totalPrice.compareTo(currentTotalPrice)!=0){
            throw new OrderException("页面已经过期，请刷新后重新尝试");
        }
        //3.验证并锁定库存（具备原子性，支付完成之后，才是真正的减库存）
        List<SkuLockVO> skuLockVOS = items.stream().map(orderItemVO -> {
            SkuLockVO skuLockVO = new SkuLockVO();
            skuLockVO.setSkuId(orderItemVO.getSkuId());
            skuLockVO.setCount(orderItemVO.getCount());
            skuLockVO.setOrderToken(orderSubmitVO.getOrderToken());
            return skuLockVO;
        }).collect(Collectors.toList());
        Resp<List<SkuLockVO>> skuLockResp = this.gmallWmsClient.checkAndLock(skuLockVOS);
        List<SkuLockVO> lockVOS = skuLockResp.getData();
        if (!CollectionUtils.isEmpty(lockVOS)) {
            throw new OrderException(JSON.toJSONString(lockVOS));
        }
        //异常（服务器宕机）：后续订单无法创建，定时释放库存


        //4.新增订单（订单状态，未付款的状态）
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        OrderEntity orderEntity=null;
        try {
            Resp<OrderEntity> orderEntityResp = this.gmallOmsClient.saveOrder(orderSubmitVO, userInfo.getUserId());
             orderEntity = orderEntityResp.getData();
        } catch (Exception e) {
            e.printStackTrace();
            //订单创建异常应该立马释放内存  feign(阻塞) 消息队列（异步）
            this.amqpTemplate.convertAndSend("ORDER-EXCHANGE","stock.unlock",orderSubmitVO.getOrderToken());
            throw new OrderException("订单保存失败，服务错误");
        }
        //5.删除购物车中的相关记录
        try {
            Map<String,Object> map=new HashMap<>();
            map.put("userId",userInfo.getUserId());
            List<Long> skuIds = items.stream().map(orderItemVO -> orderItemVO.getSkuId()).collect(Collectors.toList());
            map.put("skuIds",skuIds);
            this.amqpTemplate.convertAndSend("ORDER-EXCHANGE","cart.delete",map);
        } catch (AmqpException e) {
            e.printStackTrace();
            throw new OrderException("");
        }

        return orderEntity;
    }
}
