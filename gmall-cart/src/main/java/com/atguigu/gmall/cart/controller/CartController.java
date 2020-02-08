package com.atguigu.gmall.cart.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping("test")
    public String test(HttpServletRequest request){
        System.out.println(LoginInterceptor.getUserInfo());
//        System.out.println(request.getAttribute("userKey"));
//        System.out.println(request.getAttribute("userId"));

        return "xxxx";
    }
    @PostMapping
    public Resp<Object> addCart(@RequestBody Cart cart){
        this.cartService.addCart(cart);
        return Resp.ok(null);
    }
    @GetMapping
    public Resp<List<Cart>>  queryCarts(){
        List<Cart> carts =  this.cartService.queryCarts();
        return Resp.ok(carts);
    }

    @PostMapping("update")
    public Resp<Object> updateNum(@RequestBody Cart cart){
        this.cartService.updateNum(cart);
        return Resp.ok(null);
    }

    @PostMapping("check")
    public Resp<Object> check(@RequestBody Cart cart){
        this.cartService.check(cart);
        return Resp.ok(null);
    }

    @PostMapping("delete")
    public Resp<Object> delete(Long skuId){
        this.cartService.delete(skuId);
        return Resp.ok(null);
    }

    @GetMapping("{userId}")
    public List<Cart> queryCheckedCarts(@PathVariable("userId") Long userId){
        List<Cart> carts = this.cartService.queryCheckedCarts(userId);
        return carts;
    }
}
