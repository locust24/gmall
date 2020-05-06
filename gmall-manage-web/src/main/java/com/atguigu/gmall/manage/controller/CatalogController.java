package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsBaseCatalog1;
import com.atguigu.gmall.bean.PmsBaseCatalog2;
import com.atguigu.gmall.bean.PmsBaseCatalog3;
import com.atguigu.gmall.service.CatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author leizi
 * @create 2020-05-05 15:54
 */
@Controller
@CrossOrigin // 跨域访问注解
public class CatalogController {

    @Reference
    CatalogService catalogService;

    /**
     * 商品管理-一级分类查询
     * @return
     */
    @RequestMapping("/getCatalog1")
    @ResponseBody
    public List<PmsBaseCatalog1> getCatalog1(){
        return catalogService.getCatalog1();
    }

    /**
     * 商品管理-二级分类查询
     * @param catalog1Id
     * @return
     */
    @RequestMapping("/getCatalog2")
    @ResponseBody
    public List<PmsBaseCatalog2> getCatalog2(@RequestParam String catalog1Id){
        return catalogService.getCatalog2(catalog1Id);
    }

    /**
     * 商品管理-三级分类查询
     * @param catalog2Id
     * @return
     */
    @RequestMapping("/getCatalog3")
    @ResponseBody
    public List<PmsBaseCatalog3> getCatalog3(@RequestParam String catalog2Id){
        return catalogService.getCatalog3(catalog2Id);
    }
}
