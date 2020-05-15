package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PmsSkuInfo;

import java.util.List;

/**
 * @author leizi
 * @create 2020-05-08 11:22
 */
public interface SkuService {
    String saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuById(String skuId);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);

    List<PmsSkuInfo> getAllSku();
}
