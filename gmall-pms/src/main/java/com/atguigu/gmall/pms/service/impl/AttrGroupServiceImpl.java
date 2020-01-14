package com.atguigu.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.dao.ProductAttrValueDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    private AttrAttrgroupRelationDao attrgroupRelationDao;
    @Autowired
    private AttrDao attrDao;
    @Autowired
    private AttrGroupDao attrGroupDao;
    @Autowired
    private ProductAttrValueDao productAttrValueDao;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryGroupByCidPage(QueryCondition queryCondition, Long catId) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(queryCondition),
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id",catId)
        );

        return new PageVo(page);
    }

    @Override
    public GroupVo queryGroupVoByCid(Long gid) {
        GroupVo groupVo = new GroupVo();
        //根据gid查询组
        AttrGroupEntity groupEntity = this.attrGroupDao.selectById(gid);
        BeanUtils.copyProperties(groupEntity,groupVo);
        //查询中间表
        List<AttrAttrgroupRelationEntity> relationEntites = this.attrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", gid));
        //判断中间表是否为空
        if(CollectionUtils.isEmpty(relationEntites)){
          return groupVo;
        }
        groupVo.setRelations(relationEntites);
        //获取所有规格参数的id
        List<Long> attrIds = relationEntites.stream().map(relation -> relation.getAttrId()).collect(Collectors.toList());
        //查询规格参数
        List<AttrEntity> attrEntities = this.attrDao.selectBatchIds(attrIds);
        groupVo.setAttrEntities(attrEntities);
        return groupVo;
    }

    @Override
    public List<GroupVo> queryGroupVosByCid(Long cid) {
        //根据分类的id查询规格参数组
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", cid));
        //遍历规格参数组查询每个组下的中间关系
        return groupEntities.stream().map(attrGroupEntity -> this.queryGroupVoByCid(attrGroupEntity.getAttrGroupId())).collect(Collectors.toList());
        //查询每个组在的规格参数

    }

    @Override
    public List<ItemGroupVo> queryItemGroupVosByCidAndSpuId(Long cid, Long spuId) {
        //1.根据sku中的categoryid查询分组
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", cid));
        if(CollectionUtils.isEmpty(groupEntities)){
            return null;
        }
        return groupEntities.stream().map(group->{
            ItemGroupVo itemGroupVo = new ItemGroupVo();
            itemGroupVo.setId(group.getAttrGroupId());
            itemGroupVo.setName(group.getAttrGroupName());
            //2.遍历组到中间表查询每个组的规格参数id
            List<AttrAttrgroupRelationEntity> attrgroupRelationEntities = this.attrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", group.getAttrGroupId()));
            if(!CollectionUtils.isEmpty(attrgroupRelationEntities)){
                List<Long> attrIds = attrgroupRelationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrGroupId).collect(Collectors.toList());

                //3.根据spuid和attrid查询规格参数名及值
                List<ProductAttrValueEntity> attrValueEntities = this.productAttrValueDao.selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId).in("attr_id", attrIds));
                itemGroupVo.setBaseAttrValues(attrValueEntities);
            }
            return itemGroupVo;
        }).collect(Collectors.toList());
    }

}























