package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.PmsSkuInfo;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author leizi
 * @create 2020-05-08 11:25
 */
@Repository
public interface PmsSkuInfoMapper extends Mapper<PmsSkuInfo> {
    List<PmsSkuInfo> selectSkuSaleAttrValueListBySpu(String productId);
}
