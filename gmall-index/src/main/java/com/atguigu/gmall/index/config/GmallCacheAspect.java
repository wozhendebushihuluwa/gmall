package com.atguigu.gmall.index.config;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallCacheAspect {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(com.atguigu.gmall.index.config.GmallCache)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
        //1.获取缓存
        MethodSignature signature = (MethodSignature)proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        GmallCache annotation = method.getAnnotation(GmallCache.class);
        String prefix = annotation.value();
        Class returnType = signature.getReturnType();
        String key=prefix+ Arrays.asList(proceedingJoinPoint.getArgs());
        //2.判断数据是否为空
        Object cache = this.getCache(key, returnType);
        if(cache!=null){
            return cache;
        }
        //3.为空，加分布式锁
        String lockName = annotation.lockName();
        RLock fairLock = this.redissonClient.getFairLock(lockName + Arrays.asList(proceedingJoinPoint.getArgs()));
        fairLock.lock();
        //4.判断缓存是否为空
        Object cache1 = this.getCache(key, returnType);
        if (cache1!=null){
            fairLock.unlock();
            return cache1;
        }
        Object result = proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
        //5.将数据放入缓存，释放分布式锁
        this.stringRedisTemplate.opsForValue().set(key,JSON.toJSONString(result),annotation.timeout()+new Random().nextInt(annotation.bound()), TimeUnit.MINUTES);
        fairLock.unlock();
        return result;
    }

    private Object getCache(String key,Class returnType){
        String JsonString = this.stringRedisTemplate.opsForValue().get(key);
        if(StringUtils.isNotBlank(JsonString)){
            return JSON.parseObject(JsonString,returnType);
        }
        return null;
    }
}
