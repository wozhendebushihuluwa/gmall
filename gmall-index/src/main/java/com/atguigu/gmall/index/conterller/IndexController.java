package com.atguigu.gmall.index.conterller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.categoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("index")
public class IndexController {
    @Autowired
    private IndexService indexService;
    @GetMapping("cates")
    public Resp<List<CategoryEntity>> queryLv1Cates(){
        List<CategoryEntity> categoryEntities=this.indexService.queryLv1Cates();
        return Resp.ok(categoryEntities);
    }

    @GetMapping("cates/{pid}")
    public Resp<List<categoryVo>> queryCategorysWithSub(@PathVariable("pid")Long pid){
        List<categoryVo> categoryVos=this.indexService.queryCategorysWithSub(pid);
        return Resp.ok(categoryVos);
    }

    @GetMapping("lock")
    public Resp<Object> testLock(){
        this.indexService.testLock();
        return Resp.ok(null);
    }

    @GetMapping("read")
    public Resp<String> readLock(){
        String s=this.indexService.readLock();
        return Resp.ok(s);
    }

    @GetMapping("write")
    public Resp<String> writeLock(){
        String s=this.indexService.writeLock();
        return Resp.ok(s);
    }

    @GetMapping("latch")
    public Resp<String> latchLock(){
        String s=this.indexService.latchLock();
        return Resp.ok(s);
    }

    @GetMapping("countdown")
    public Resp<String> countdownLock() throws InterruptedException {
        String s=this.indexService.countdownLock();
        return Resp.ok(s);
    }
}