package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author leizi
 * @create 2020-05-16 11:09
 */
@Controller
@CrossOrigin
public class CartController {

    @Reference
    CartService cartService;

    @Reference
    SkuService skuService;

    @RequestMapping("/checkCart")
    public String checkCart(String isChecked, String skuId, HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {

        // 判断用户登录状态
        String memberId = "1";

        // 调用服务，修改状态
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setIsChecked(isChecked);
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setMemberId(memberId);

        cartService.checkCart(omsCartItem);

        // 将最新的数据从缓存中查出，渲染给内嵌页
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        modelMap.put("cartList",omsCartItems);

        return "cartListInner";
    }

    @RequestMapping("/cartList")
    public String cartList(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {

        List<OmsCartItem> omsCartItems = new ArrayList<>();

        // 查询用户的购物车数据
        String memberId = "1";

        if (StringUtils.isNotBlank(memberId)) {
            // 用户已登录查询DB中购物车数据(查询缓存)
            omsCartItems = cartService.cartList(memberId);
        } else {
            // 未登录查询Cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)) {
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
            }
        }

        // 计算购物车单件商品的总价格（商品价格 * 商品数量）
        for (OmsCartItem omsCartItem : omsCartItems) {
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
        }

        modelMap.put("cartList", omsCartItems);

        // 被勾选的商品的总价格
        BigDecimal totalAmount = getTotalAmount(omsCartItems);
        modelMap.put("totalAmount", totalAmount);

        return "cartList";
    }

    /**
     * 被勾选的商品的总价格
     * @param omsCartItems
     * @return
     */
    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {

        BigDecimal totalAmount = new BigDecimal("0");

        for (OmsCartItem omsCartItem : omsCartItems) {
            // 商品被选中才被计算
            if ("1".equals(omsCartItem.getIsChecked())) {

                totalAmount.add(omsCartItem.getPrice());
            }
        }

        return totalAmount;
    }

    @RequestMapping("/addToCart")
    public String addToCart(String skuId, BigDecimal quantity, HttpServletRequest request, HttpServletResponse response) {

        List<OmsCartItem> omsCartItems = new ArrayList<>();

        // 根据商品服务查询商品信息
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId);

        // 将商品信息封装成购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();

        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(pmsSkuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(pmsSkuInfo.getCatalog3Id());
        omsCartItem.setProductId(pmsSkuInfo.getProductId());
        omsCartItem.setProductName(pmsSkuInfo.getSkuName());
        omsCartItem.setProductPic(pmsSkuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("00000000");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(quantity);

        // 判断用户是否登录
        String memberId = "1";  // 测试使用

        if (StringUtils.isBlank(memberId)) {
            // 未登录,操作Cookie
            // 1.获取Cookie中数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);

            // 2.判断Cookie是否为空
            if (StringUtils.isBlank(cartListCookie)) {
                // 为空直接添加
                omsCartItems.add(omsCartItem);
            } else {
                // 不为空
                // 解析为购物车数据
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                // 判断新添加数据在Cookie中是否存在
                boolean exists = if_cart_exists(omsCartItems, omsCartItem);
                if (exists) {
                    // 存在数量更新
                    for (OmsCartItem cartItem : omsCartItems) {
                        if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                            cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                        }
                    }

                } else {
                    // 不存在则新增
                    omsCartItems.add(omsCartItem);
                }

                // 更新Cookie
                CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItems), 60 * 60 * 72, true);
            }
        } else {
            // 已登录
            // 1.查询用户的购物车是否存在指定商品
            OmsCartItem omsCartItemFromDB = cartService.ifCartExistByUser(memberId, skuId);

            // 2.判断商品是否存在用户购物车中
            if (omsCartItemFromDB == null) {
                // 用户购物车中没有该商品
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname("baby");
                omsCartItem.setQuantity(quantity);

                // 添加商品
                cartService.addCart(omsCartItem);
            } else {
                // 用户购物车存在该商品，更新商品数量
                omsCartItemFromDB.setQuantity(omsCartItemFromDB.getQuantity().add(omsCartItem.getQuantity()));
                // 更新购物车内该商品信息
                cartService.updateCart(omsCartItemFromDB);
            }
            // 同步缓存
            cartService.flushCartCache(memberId);
        }

        return "redirect:/success.html";
    }

    private boolean if_cart_exists(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        boolean exists = false;

        for (OmsCartItem cartItem : omsCartItems) {
            String productId = cartItem.getProductId();
            if (productId.equals(omsCartItem.getProductId())) {
                exists = true;
                return exists;
            }
        }
        return exists;
    }
}
