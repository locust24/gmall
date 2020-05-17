package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OmsCartItem;

import java.util.List;

/**
 * @author leizi
 * @create 2020-05-16 11:49
 */
public interface CartService {
    OmsCartItem ifCartExistByUser(String memberId, String skuId);

    List<OmsCartItem> flushCartCache(String memberId);

    void updateCart(OmsCartItem omsCartItemFromDB);

    void addCart(OmsCartItem omsCartItem);

    List<OmsCartItem> cartList(String userId);

    void checkCart(OmsCartItem omsCartItem);
}
