package com.atguigu.gmall.oms.dao;

import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author zhanghuixin
 * @email zhx@atguigu.com
 * @date 2020-02-10 09:25:28
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {
	
}
