package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsBaseSaleAttr;
import com.atguigu.gmall.bean.PmsProductImage;
import com.atguigu.gmall.bean.PmsProductInfo;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.manage.util.PmsUploadUtil;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author leizi
 * @create 2020-05-06 10:03
 */
@Controller
@CrossOrigin
public class SpuController {

    @Reference
    SpuService spuService;

    /**
     * 添加sku查询图片
     * @param spuId
     * @return
     */
    @RequestMapping("/spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(@RequestParam String spuId){
        return spuService.spuImageList(spuId);
    }

    /**
     * 查询销售属性、属性值
     * @param spuId
     * @return
     */
    @RequestMapping("/spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(@RequestParam String spuId){
        return spuService.spuSaleAttrList(spuId);
    }

    /**
     * spu图片上传fastdfs服务器
     * @param multipartFile
     * @return
     */
    @RequestMapping("/fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){
        String imgUrl = PmsUploadUtil.uploadImage(multipartFile);
        return imgUrl;
    }

    /**
     * spu保存、修改
     * @param pmsProductInfo
     * @return
     */
    @RequestMapping("/saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){
        return spuService.saveSpuInfo(pmsProductInfo);
    }

    /**
     * 查询spu
     * @param catalog3Id
     * @return
     */
    @RequestMapping("/spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(@RequestParam String catalog3Id){

        return spuService.spuList(catalog3Id);
    }

    /**
     * 查询销售属性字典表
     * @return
     */
    @RequestMapping("/baseSaleAttrList")
    @ResponseBody
    public List<PmsBaseSaleAttr> baseSaleAttrList(){
        return spuService.baseSaleAttrList();
    }

}
