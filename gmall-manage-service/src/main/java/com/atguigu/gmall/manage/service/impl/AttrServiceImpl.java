package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsBaseAttrValue;
import com.atguigu.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.atguigu.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

/**
 * @author leizi
 * @create 2020-05-05 18:24
 */
@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;

    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;

    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);

        // 根据BaseAttrInfo的ID获取BaseAttrValue属性值
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfos) {
            List<PmsBaseAttrValue> baseAttrValues = new ArrayList<>();

            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());

            List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);

            for (PmsBaseAttrValue baseAttrValue : pmsBaseAttrValues) {
                baseAttrValues.add(baseAttrValue);
            }

            baseAttrInfo.setAttrValueList(baseAttrValues);
        }

        return pmsBaseAttrInfos;
    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        return pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
    }

    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {

        try {
            if (!StringUtils.isBlank(pmsBaseAttrInfo.getId())) {
                // id存在即修改
                Example example = new Example(PmsBaseAttrInfo.class);
                example.createCriteria().andEqualTo("id", pmsBaseAttrInfo.getId());
                pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo, example);

                PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.delete(pmsBaseAttrValue);

                // 公共方法
                insertAttrValue(pmsBaseAttrInfo);
            } else {
                // id不存在即新添加
                pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);

                // 公共方法
                insertAttrValue(pmsBaseAttrInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }

        return "success";
    }

    /**
     * 商品属性值新增-公共方法
     *
     * @param pmsBaseAttrInfo
     */
    private void insertAttrValue(PmsBaseAttrInfo pmsBaseAttrInfo) {
        try {
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            // 根据attrInfo中封装数据插入attrValue数据
            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                // 主键回显
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());

                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
