package com.atguigu.gmall.index.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.categoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndexService {
    @Autowired
    private GmallPmsClient gmallPmsClient;

    public List<CategoryEntity> queryLv1Cates() {
        Resp<List<CategoryEntity>> categoriesByLevelOrPid = this.gmallPmsClient.queryCategoriesByLevelOrPid(1, null);
        List<CategoryEntity> categoryEntities = categoriesByLevelOrPid.getData();
        return categoryEntities;
    }

    public List<categoryVo> queryCategorysWithSub(Long pid) {
        Resp<List<categoryVo>> categoryWithSub = this.gmallPmsClient.queryCategoryWithSub(pid);
        List<categoryVo> categoryVos = categoryWithSub.getData();
        return categoryVos;
    }
}
