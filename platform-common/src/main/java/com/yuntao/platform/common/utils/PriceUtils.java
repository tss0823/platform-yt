package com.yuntao.platform.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class PriceUtils {
    public static String feng2DotYuan(Integer price) {
        if(price==null){
            price = 0;
        }
        return new DecimalFormat("0.00").format(price * 0.01);
    }
    public static String feng2DotYuan(Long price) {
        return feng2DotYuan(price.intValue());
    }
    
    public static String fen2yuan(Long num){
    	if(num==null){
    		num = 0l;
        }
        return new DecimalFormat("0.00").format(num * 0.01);
    }
    public static String feng2DotYuan(String price) {
        if(StringUtils.isBlank(price)){
            price = "";
        }
        return new DecimalFormat("0.00").format(NumberUtils.toInt(price) * 0.01);
    }
    
    public static String fenRun2Yuan(Long price) {
    	if(price==null){
    		return "0";
    	}
    	long total = price.longValue();
    	BigDecimal big = new BigDecimal(total);
    	BigDecimal danwei = new BigDecimal(NumberUtil.MILLION*100);
    	return big.divide(danwei).toString();
    }
    
    public static void main(String[] args){
    	System.out.println(fenRun2Yuan(100000000l));
    }
}
