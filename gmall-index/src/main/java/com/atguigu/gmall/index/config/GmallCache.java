package com.atguigu.gmall.index.config;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {

    //自定义缓存的key是什么
    String value() default "";

    //自定义缓存的有效时间,单位是分钟
    int timeout() default 30;

    //随机时间的范围，为了防止雪崩
    int bound() default 5;

    //自定义分布锁的名称
    String lockName() default "lock";


}
