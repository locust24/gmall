package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.cart.mapper.OmsCartItemMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

/**
 * @author leizi
 * @create 2020-05-16 16:47
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    OmsCartItemMapper omsCartItemMapper;

    @Autowired
    RedisUtil redisUtil;

    /**
     * 根据用户id与商品id查询该商品在用户购物车中是否存在
     *
     * @param memberId 用户id
     * @param skuId    商品id
     * @return 商品数据
     */
    @Override
    public OmsCartItem ifCartExistByUser(String memberId, String skuId) {
        OmsCartItem omsCartItemExist = null;
        try {
            OmsCartItem omsCartItem = new OmsCartItem();
            omsCartItem.setMemberId(memberId);
            omsCartItem.setProductSkuId(skuId);

            omsCartItemExist = omsCartItemMapper.selectOne(omsCartItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return omsCartItemExist;
    }

    /**
     * 用户购物车内商品更新，同步缓存
     *
     * @param memberId
     */
    @Override
    public List<OmsCartItem> flushCartCache(String memberId) {

        Jedis jedis = null;
        List<OmsCartItem> omsCartItems = null;
        try {
            OmsCartItem omsCartItem = new OmsCartItem();
            omsCartItem.setMemberId(memberId);
            omsCartItems = omsCartItemMapper.select(omsCartItem);

            // 同步到redis缓存中
            jedis = redisUtil.getJedis();

            Map<String, String> map = new HashMap<>();
            for (OmsCartItem cartItem : omsCartItems) {
                cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
                map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
            }

            // 先删除指定key的数据
            jedis.del("user:" + memberId + ":cart");
            // 再添加数据
            jedis.hmset("user:" + memberId + ":cart", map);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        return omsCartItems;

    }

    /**
     * 用户购物车中存在该商品，更新商品信息
     *
     * @param omsCartItemFromDB 更新的商品信息
     */
    @Override
    public void updateCart(OmsCartItem omsCartItemFromDB) {
        // 根据主键更新
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("id", omsCartItemFromDB.getId());
        omsCartItemMapper.updateByExampleSelective(omsCartItemFromDB, example);
    }

    /**
     * 添加商品到用户购物车
     *
     * @param omsCartItem 商品数据
     */
    @Override
    public void addCart(OmsCartItem omsCartItem) {

        // 校验用户id是否为空
        if (StringUtils.isNotBlank(omsCartItem.getMemberId())) {

            omsCartItemMapper.insertSelective(omsCartItem);
        } else {
            // 用户id为空不能添加数据，返回给前台信息（工作中）
        }

    }

    /**
     * 查询购物车中数据
     *
     * @param memberId 用户id
     * @return 购物车数据
     */
    @Override
    public List<OmsCartItem> cartList(String memberId) {
        Jedis jedis = null;
        List<OmsCartItem> omsCartItems = null;
        try {
            jedis = redisUtil.getJedis();

            String skuKey = "user:" + memberId + ":cart";

            omsCartItems = new ArrayList<>();

            // 1.查询缓存中是否存在用户的购物车数据
            List<String> hvals = jedis.hvals(skuKey);
            if (hvals.isEmpty()) {
                // 2.存在直接返回
                for (String hval : hvals) {
                    OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                    omsCartItems.add(omsCartItem);
                }
            } else {
                // 3.不存在查询DB
                omsCartItems = flushCartCache(memberId);
            }
        } catch (Exception e) {
            e.printStackTrace();

            // 工作中，异常处理机制，记录系统日志
            // String message = e.getMessage();
            // logService.addErrorLog(message);

            // 出现异常返回null，避免方法调用处等待
            return null;
        } finally {
            jedis.close();
        }

        return omsCartItems;
    }

    /**
     * 购物车内商品选中状态更新
     * @param omsCartItem
     */
    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId",omsCartItem.getMemberId()).andEqualTo("productSkuId",omsCartItem.getProductSkuId());
        omsCartItemMapper.updateByExampleSelective(omsCartItem, example);

        // 同步缓存
        flushCartCache(omsCartItem.getMemberId());
    }
}
