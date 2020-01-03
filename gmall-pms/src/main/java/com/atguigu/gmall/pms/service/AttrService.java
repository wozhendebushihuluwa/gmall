package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.AttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 商品属性
 *
 * @author zhanghuixin
 * @email zhx@atguigu.com
 * @date 2020-01-02 14:46:20
 */
public interface AttrService extends IService<AttrEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo queryAttrByCidOrTypePage(QueryCondition queryCondition, Long cid, Integer type);

    void saveAttrVo(AttrVo attrVo);
}

