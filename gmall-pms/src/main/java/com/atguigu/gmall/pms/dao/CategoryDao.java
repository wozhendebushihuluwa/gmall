package com.atguigu.gmall.pms.dao;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.categoryVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品三级分类
 * 
 * @author zhanghuixin
 * @email zhx@atguigu.com
 * @date 2020-01-02 14:46:19
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {

    List<categoryVo> queryCategoryWithSub(Long pid);
}
