package com.atguigu.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.SkuInfoDao;
import com.atguigu.gmall.pms.dao.SkuSaleAttrValueDao;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuSaleAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {
    @Autowired
    private SkuInfoDao skuInfoDao;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public List<SkuSaleAttrValueEntity> querySaleAttrValueBySpuId(Long spuId) {

        //1.根据sku中的skuid查询skus
        List<SkuInfoEntity> skuInfoEntity = this.skuInfoDao.selectList(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        if(CollectionUtils.isEmpty(skuInfoEntity)){
          return null;
        }
        //2.根据sku查询skuids
        List<Long> skuIds = skuInfoEntity.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        //3.根据skuids查询销售属性
        return this.list(new QueryWrapper<SkuSaleAttrValueEntity>().in("sku_id",skuIds));
    }

}