package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.fasterxml.jackson.databind.util.BeanUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    private AttrAttrgroupRelationDao attrgroupRelationDao;
    @Autowired
    private AttrDao attrDao;
    @Autowired
    private AttrGroupDao attrGroupDao;
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

}























