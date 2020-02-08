package com.atguigu.gmall.order.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.gmall.order.feign.GmallPmsClient;
import com.atguigu.gmall.order.feign.GmallSmsClient;
import com.atguigu.gmall.order.feign.GmallUmsClient;
import com.atguigu.gmall.order.feign.GmallWmsClient;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderComfirmVO;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public OrderComfirmVO confirm() {
        OrderComfirmVO orderComfirmVO = new OrderComfirmVO();

        //获取用户的登录信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //获得用户地址信息（远程连接）
        Resp<List<MemberReceiveAddressEntity>> userResp = this.gmallUmsClient.queryAddressesByUserId(userInfo.getUserId());
        List<MemberReceiveAddressEntity> addressEntities = userResp.getData();
        orderComfirmVO.setAddresses(addressEntities);
        //获取订单详情列表（远程接口）
        //远程调用购物车的接口获取选中的购物车记录

        orderComfirmVO.setOrderItems(null);
        //获取用户积分信息ums
        orderComfirmVO.setBounds(null);
        //防止重复提交的唯一标志
        orderComfirmVO.setOrderToken(null);

        orderComfirmVO.setWeight(null);

        return null;
    }
}
