package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.categoryVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 商品三级分类
 *
 * @author zhanghuixin
 * @email zhx@atguigu.com
 * @date 2020-01-02 14:46:19
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageVo queryPage(QueryCondition params);

    List<CategoryEntity> queryCategoriesByLevelOrPid(Integer level, Long pid);

    List<categoryVo> queryCategoryWithSub(Long pid);
}

