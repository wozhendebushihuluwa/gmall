package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderComfirmVO {

    private List<MemberReceiveAddressEntity> addresses;

    private List<OrderItemVO> orderItems;

    private Long bounds;//积分信息

    private String orderToken;//防止重复提交

    private BigDecimal weight;
}
