package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsBaseAttrValue;
import com.atguigu.gmall.service.AttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author leizi
 * @create 2020-05-05 18:12
 */
@Controller
@CrossOrigin
public class AttrController {

    @Reference
    AttrService attrService;

    /**
     * 商品平台属性管理-查询属性
     *
     * @param catalog3Id
     * @return
     */
    @RequestMapping("/attrInfoList")
    @ResponseBody
    public List<PmsBaseAttrInfo> attrInfoList(@RequestParam String catalog3Id) {
        return attrService.attrInfoList(catalog3Id);
    }

    /**
     * 商品平台属性管理-查询属性值
     *
     * @param attrId
     * @return
     */
    @RequestMapping("/getAttrValueList")
    @ResponseBody
    public List<PmsBaseAttrValue> getAttrValueList(@RequestParam String attrId) {

        return attrService.getAttrValueList(attrId);
    }

    /**
     * 商品平台属性管理-添加保存修改属性、属性值
     * @param pmsBaseAttrInfo
     * @return
     */
    @RequestMapping("/saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo) {

        return attrService.saveAttrInfo(pmsBaseAttrInfo);
    }

}
