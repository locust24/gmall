package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.bean.PmsSkuAttrValue;
import com.atguigu.gmall.bean.PmsSkuImage;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.bean.PmsSkuSaleAttrValue;
import com.atguigu.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuImageMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.GmallRedissonConfig;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author leizi
 * @create 2020-05-08 11:23
 */
@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public String saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        // 保存skuInfo值
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);

        // 保存image
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(pmsSkuInfo.getId());
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }

        // 保存销售属性值
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(pmsSkuInfo.getId());
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }

        // 保存平台属性值
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }

        return "success";
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);

        return pmsSkuInfos;
    }

    /**
     * 获取全部sku数据
     *
     * @return
     */
    @Override
    public List<PmsSkuInfo> getAllSku() {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            String skuInfoId = pmsSkuInfo.getId();

            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuInfoId);
            List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);

            pmsSkuInfo.setSkuAttrValueList(pmsSkuAttrValues);
        }

        return pmsSkuInfos;
    }

    /**
     * item查询页面信息-直接从数据库查询数据版本
     *
     * @param skuId
     * @return
     */
    public PmsSkuInfo getSkuByIdFormDB(String skuId) {

        // sku商品对象
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        // 图片列表
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        skuInfo.setSkuImageList(pmsSkuImageMapper.select(pmsSkuImage));

        return skuInfo;
    }

    /**
     * item查询页面信息-使用缓存版本
     *
     * @param skuId
     * @return
     */
    @Override
    public PmsSkuInfo getSkuById(String skuId) {

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();

        // 获取锁
        RLock lock = redissonClient.getLock("lock");

        // 连接缓存
        Jedis jedis = redisUtil.getJedis();

        // 查询缓存
        String skuKey = "sku:" + skuId + ":info";
        String skuValue = jedis.get(skuKey);

        if (StringUtils.isNotBlank(skuValue)) {
            // 命中查询redis
            pmsSkuInfo = JSON.parseObject(skuValue, PmsSkuInfo.class);
            jedis.close();
        } else {

            try {
                // 未命中设置分布式锁
                // 加锁
                lock.lock(10, TimeUnit.SECONDS);

                // 有权在10秒的过期时间内访问数据库
                // 如果缓存中没有，查数据库
                pmsSkuInfo = getSkuByIdFormDB(skuId);

                if (pmsSkuInfo != null) {
                    // 数据库找到数据，缓存到redis
                    jedis.set(skuKey, JSON.toJSONString(pmsSkuInfo));
                } else {
                    // 数据库中也没有找到数据，返回null
                    // null保存到redis，并设置过期时间,防止缓存穿透
                    jedis.setex(skuKey, 60 * 3, JSON.toJSONString(""));
                }
            } finally {
                jedis.close();
                lock.unlock();
            }
        }

        return pmsSkuInfo;
    }

}
