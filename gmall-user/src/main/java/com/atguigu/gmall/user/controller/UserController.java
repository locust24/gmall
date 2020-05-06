package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
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
