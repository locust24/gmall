package com.atguigu.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author leizi
 * @create 2020-05-03 17:56
 */
@Controller
public class UserController {
    @Reference
    UserService userService;

    @RequestMapping("/findAll")
    @ResponseBody
    public List<UmsMember> findAll(){
        return userService.getAllUser();
    }

    @RequestMapping("/hello")
    @ResponseBody
    public String helloController(){
        return "Test Success";
    }
}
