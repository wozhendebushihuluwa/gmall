package com.atguigu.gmall.index.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.config.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.categoryVo;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexService {

    private static final String KEY_PREFIX="index:cates:";
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    public List<CategoryEntity> queryLv1Cates() {
        Resp<List<CategoryEntity>> categoriesByLevelOrPid = this.gmallPmsClient.queryCategoriesByLevelOrPid(1, null);
        List<CategoryEntity> categoryEntities = categoriesByLevelOrPid.getData();
        return categoryEntities;
    }
    @GmallCache(value = "index:cates:",timeout = 7200,bound = 100,lockName = "lock")
    public List<categoryVo> queryCategorysWithSub(Long pid) {
//        // 获取缓存的数据
//        String JsonString = this.stringRedisTemplate.opsForValue().get(KEY_PREFIX+pid);
//        //有直接返回
//        if(StringUtils.isNotBlank(JsonString)){
//            return JSON.parseArray(JsonString,categoryVo.class);
//        }
//        //加分布式锁
//        RLock lock = this.redissonClient.getLock("lock"+pid);
//        lock.lock();
//
//        // 获取缓存的数据
//        String JsonString2 = this.stringRedisTemplate.opsForValue().get(KEY_PREFIX+pid);
//        //有直接返回
//        if(StringUtils.isNotBlank(JsonString2)){
//            lock.unlock();
//            return JSON.parseArray(JsonString2,categoryVo.class);
//        }

        //没有，远程调用查询
        Resp<List<categoryVo>> categoryWithSub = this.gmallPmsClient.queryCategoryWithSub(pid);
        List<categoryVo> categoryVos = categoryWithSub.getData();
        //查询完成后放入缓存
//        this.stringRedisTemplate.opsForValue().set(KEY_PREFIX+pid,JSON.toJSONString(categoryVos),5+new Random().nextInt(5), TimeUnit.DAYS);
//        lock.unlock();
        return categoryVos;
    }
    public  void testLock() {
        //加锁
        RLock lock = this.redissonClient.getLock("lock");
        lock.lock();

        String num = this.stringRedisTemplate.opsForValue().get("num");
        if (num==null){
            return ;
        }
        Integer integer = new Integer(num);
        this.stringRedisTemplate.opsForValue().set("num",String.valueOf(++integer));
        lock.unlock();
    }









    public  void testLock1() {
        String uuid = UUID.randomUUID().toString();
        //所有请求执行setnx，返回值为true，说明获取到锁
        Boolean lock = this.stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid,5,TimeUnit.SECONDS);
        if (lock){
            String num = this.stringRedisTemplate.opsForValue().get("num");
            if (num==null){
                return ;
            }
            Integer integer = new Integer(num);

            this.stringRedisTemplate.opsForValue().set("num",String.valueOf(++integer));
            //执行完成后要释放锁
            String script="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            this.stringRedisTemplate.execute(new DefaultRedisScript<>(script,Long.class), Arrays.asList("lock"),uuid);

//            String lock1 = this.stringRedisTemplate.opsForValue().get("lock");
//            if(StringUtils.equals(lock1,uuid)){
//                this.stringRedisTemplate.delete("lock");
//            }
        }else{
            //如果没有获取到锁，重试
            try {
                TimeUnit.SECONDS.sleep(1);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String readLock() {
        RReadWriteLock readwritelock = this.redissonClient.getReadWriteLock("readwritelock");
        readwritelock.readLock().lock(10,TimeUnit.SECONDS);
        String msg = this.stringRedisTemplate.opsForValue().get("msg");
//        readwritelock.readLock().unlock();
        return "我读到了"+msg;
    }

    public String writeLock() {
        RReadWriteLock readwritelock = this.redissonClient.getReadWriteLock("readwritelock");
        readwritelock.writeLock().lock(10,TimeUnit.SECONDS);
        String uuid = UUID.randomUUID().toString();
        this.stringRedisTemplate.opsForValue().set("msg", uuid);
//        readwritelock.writeLock().unlock();
        return "我写了"+uuid;
    }

    public String countdownLock() throws InterruptedException {

        RCountDownLatch countdown = this.redissonClient.getCountDownLatch("countdown");
        countdown.trySetCount(6);

        countdown.await();

        return "班长锁门！";
    }

    public String latchLock() {
        RCountDownLatch countdown = this.redissonClient.getCountDownLatch("countdown");
        countdown.countDown();
        return "数量减少一";
    }
}
