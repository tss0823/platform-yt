package com.yuntao.platform.common.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by shengshan.tang on 2016/9/3.
 */
public class OrderUtils {

    /**
     * 生成订单号
     *
     * @param prefix
     * @param userId
     * @return
     */
    public static String genOrderNo(String prefix, Long userId) {
        String timeMillis = "" + System.currentTimeMillis();
        int leftLen = timeMillis.length() / 2;
        String leftStr = StringUtils.left(timeMillis, leftLen);
        String genUserId = NumberUtil.formatUserId(userId);
        String orderNo = prefix + leftStr + genUserId + StringUtils.right(timeMillis, timeMillis.length() - leftLen);
        return orderNo;
    }

    public static void main(String[] args) {

        String orderNo = genOrderNo("KC", 88888888L);
        System.out.printf("orderNo=" + orderNo);
    }
}
