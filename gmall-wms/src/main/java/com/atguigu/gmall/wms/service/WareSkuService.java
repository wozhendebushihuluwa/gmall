package com.atguigu.gmall.wms.service;

import com.atguigu.gmall.wms.vo.SkuLockVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 商品库存
 *
 * @author zhanghuixin
 * @email zhx@atguigu.com
 * @date 2020-01-02 16:36:01
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageVo queryPage(QueryCondition params);

    List<SkuLockVO> checkAndLock(List<SkuLockVO> skuLockVOS);
}

