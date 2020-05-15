package com.atguigu.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsSearchSkuInfo;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceImplApplicationTests {

    @Reference
    SkuService skuService;

    @Autowired
    JestClient jestClient;

    @Test
    public void contextLoads() throws IllegalAccessException, IOException, InvocationTargetException {
        put();
    }

    public void put() throws IOException, InvocationTargetException, IllegalAccessException {

        // 查询mysql数据
        List<PmsSkuInfo> pmsSkuInfoList = new ArrayList<>();

        pmsSkuInfoList = skuService.getAllSku();

        // 转化为es的数据结构
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();

            BeanUtils.copyProperties(pmsSkuInfo, pmsSearchSkuInfo);

            pmsSearchSkuInfo.setId(Long.parseLong(pmsSkuInfo.getId()));

            pmsSearchSkuInfos.add(pmsSearchSkuInfo);

        }

        // 导入es
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            Index put = new Index.Builder(pmsSearchSkuInfo).index("gmallpms").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId() + "").build();
            jestClient.execute(put);
        }

    }

}
