package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.sms.entity.SeckillSessionEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 秒杀活动场次
 *
 * @author zhanghuixin
 * @email zhx@atguigu.com
 * @date 2020-01-05 15:27:04
 */
public interface SeckillSessionService extends IService<SeckillSessionEntity> {

    PageVo queryPage(QueryCondition params);
}

