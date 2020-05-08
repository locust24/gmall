package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author leizi
 * @create 2020-05-06 10:13
 */
@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    SpuMapper spuMapper;

    @Autowired
    PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;

    @Autowired
    PmsProductSaleAttrMapper pmsProductSaleAttrMapper;

    @Autowired
    PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;

    @Autowired
    PmsProductImageMapper pmsProductImageMapper;


    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {
        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        return spuMapper.select(pmsProductInfo);
    }

    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        return pmsBaseSaleAttrMapper.selectAll();
    }

    @Override
    public String saveSpuInfo(PmsProductInfo pmsProductInfo) {
        try {
            // 保存spu基本信息
            spuMapper.insertSelective(pmsProductInfo);

            // 保存销售属性信息
            List<PmsProductSaleAttr> spuSaleAttrList = pmsProductInfo.getSpuSaleAttrList();
            for (PmsProductSaleAttr productSaleAttr : spuSaleAttrList) {
                productSaleAttr.setProductId(pmsProductInfo.getId());
                pmsProductSaleAttrMapper.insertSelective(productSaleAttr);

                // 保存商品属性值信息
                List<PmsProductSaleAttrValue> spuSaleAttrValueList = productSaleAttr.getSpuSaleAttrValueList();
                for (PmsProductSaleAttrValue pmsProductSaleAttrValue : spuSaleAttrValueList) {
                    // spuID
                    pmsProductSaleAttrValue.setProductId(pmsProductInfo.getId());
                    // 销售属性字典表ID
                    pmsProductSaleAttrValue.setSaleAttrId(productSaleAttr.getSaleAttrId());
                    pmsProductSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);
                }

            }
            // 保存图片信息
            List<PmsProductImage> spuImageList = pmsProductInfo.getSpuImageList();
            for (PmsProductImage pmsProductImage : spuImageList) {
                pmsProductImage.setProductId(pmsProductInfo.getId());
                pmsProductImageMapper.insertSelective(pmsProductImage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return "success";
    }

    @Override
    public List<PmsProductImage> spuImageList(String spuId) {

        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        return pmsProductImageMapper.select(pmsProductImage);
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> productSaleAttrs = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);

        PmsProductSaleAttrValue saleAttrValue = new PmsProductSaleAttrValue();
        for (PmsProductSaleAttr productSaleAttr : productSaleAttrs) {
            // 设置saleAttrID
            saleAttrValue.setSaleAttrId(productSaleAttr.getSaleAttrId());
            saleAttrValue.setProductId(spuId);
            List<PmsProductSaleAttrValue> select = pmsProductSaleAttrValueMapper.select(saleAttrValue);
            productSaleAttr.setSpuSaleAttrValueList(select);
        }
        return productSaleAttrs;
    }

}
