package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.GroupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 属性分组
 *
 * @author zhanghuixin
 * @email zhx@atguigu.com
 * @date 2020-01-02 14:46:20
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo queryGroupByCidPage(QueryCondition queryCondition, Long catId);

    GroupVo queryGroupVoByCid(Long gid);

    List<GroupVo> queryGroupVosByCid(Long cid);
}

