package com.atguigu.gmall.order.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderComfirmVO;
import com.atguigu.gmall.order.vo.OrderItemVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
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
    private ThreadPoolExecutor threadPoolExecutor;

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
}
