package com.atguigu.gmall.item.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.sms.api.GmallSmsApi;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.api.GmallWmsApi;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemService {
    @Autowired
    private GmallPmsApi gmallPmsApi;
    @Autowired
    private GmallWmsApi gmallWmsApi;
    @Autowired
    private GmallSmsApi gmallSmsApi;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    public ItemVo queryItemVo(Long skuId) {
        ItemVo itemVo = new ItemVo();

        itemVo.setSkuId(skuId);
        //根据skuid查询sku
        CompletableFuture<SkuInfoEntity> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsApi.querySkuById(skuId);
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity == null) {
                return null;
            }
            itemVo.setWeight(skuInfoEntity.getWeight());
            itemVo.setSkuTitle(skuInfoEntity.getSkuTitle());
            itemVo.setSkuSubTitle(skuInfoEntity.getSkuTitle());
            itemVo.setPrice(skuInfoEntity.getPrice());
            return skuInfoEntity;
        },threadPoolExecutor);

        //根据sku中categoryid查询分类
        CompletableFuture<Void> categoryCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<CategoryEntity> categoryEntityResp = this.gmallPmsApi.queryCategoryById(skuInfoEntity.getCatalogId());
            CategoryEntity categoryEntity = categoryEntityResp.getData();
            if (categoryEntity != null) {
                itemVo.setCategoryId(categoryEntity.getCatId());
                itemVo.setCategoryName(categoryEntity.getName());
            }
        },threadPoolExecutor);

        //根据sku中的brandId查询品牌
        CompletableFuture<Void> brandCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<BrandEntity> brandEntityResp = this.gmallPmsApi.queryBrandById(skuInfoEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResp.getData();
            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getBrandId());
                itemVo.setBrandName(brandEntity.getName());
            }
        },threadPoolExecutor);
        //根据sku中的spuid查询spu
        CompletableFuture<Void> spuCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsApi.querySpuById(skuInfoEntity.getSpuId());
            SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();
            if (spuInfoEntity != null) {
                itemVo.setSpuId(spuInfoEntity.getId());
                itemVo.setSpuName(spuInfoEntity.getSpuName());
            }
        },threadPoolExecutor);

        //根据skuid查询图片
        CompletableFuture<Void> imagesCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<SkuImagesEntity>> imagesResp = this.gmallPmsApi.queryImagesBySkuid(skuId);
            List<SkuImagesEntity> imagesEntities = imagesResp.getData();
            itemVo.setImages(imagesEntities);
        },threadPoolExecutor);

        //根据skuid查询库存信息
        CompletableFuture<Void> wareCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<WareSkuEntity>> wareSkuResp = this.gmallWmsApi.queryWareSkuBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareSkuResp.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
            }
        },threadPoolExecutor);

        //根据skuid查询营销信息：积分，打折，满减
        CompletableFuture<Void> itemSalesCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<ItemSaleVo>> itemSalesResp = this.gmallSmsApi.queryItemSaleVoBySkuid(skuId);
            List<ItemSaleVo> itemSalesVos = itemSalesResp.getData();
            itemVo.setSales(itemSalesVos);
        },threadPoolExecutor);

        //根据spu中的spuid查询描述信息
        CompletableFuture<Void> descCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.gmallPmsApi.querySpuDescBySpuId(skuInfoEntity.getSpuId());
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescEntityResp.getData();
            if (spuInfoDescEntity != null && StringUtils.isNotBlank(spuInfoDescEntity.getDecript())) {
                itemVo.setDesc(Arrays.asList(StringUtils.split(spuInfoDescEntity.getDecript(), ",")));
            }
        },threadPoolExecutor);

        //1.根据sku中的categoryid查询分组
        //2.遍历组到中间表查询每个组的规格参数id
        //3.根据spuid和attrid查询规格参数名及值
        CompletableFuture<Void> groupCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<List<ItemGroupVo>> groupResp = this.gmallPmsApi.queryItemGroupVosByCidAndSpuId(skuInfoEntity.getCatalogId(), skuInfoEntity.getSpuId());
            List<ItemGroupVo> itemGroupVos = groupResp.getData();
            itemVo.setGroupVos(itemGroupVos);
        },threadPoolExecutor);

        //1.根据sku中的skuid查询skus
        //2.根据sku查询skuids
        //3.根据skuids查询销售属性
        CompletableFuture<Void> skuSaleAttrValueCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<List<SkuSaleAttrValueEntity>> skuSaleAttrValueResp = this.gmallPmsApi.querySaleAttrValueBySpuId(skuInfoEntity.getSpuId());
            List<SkuSaleAttrValueEntity> attrValueEntities = skuSaleAttrValueResp.getData();
            itemVo.setSaleAttrValues(attrValueEntities);
        },threadPoolExecutor);

        CompletableFuture.allOf(categoryCompletableFuture,brandCompletableFuture,spuCompletableFuture,
                imagesCompletableFuture,wareCompletableFuture,itemSalesCompletableFuture,
                descCompletableFuture,groupCompletableFuture,skuSaleAttrValueCompletableFuture).join();
        return itemVo;
    }
}
