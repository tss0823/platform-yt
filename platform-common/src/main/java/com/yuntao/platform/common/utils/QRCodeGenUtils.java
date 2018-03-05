package com.yuntao.platform.common.utils;

/**
 * Created by shan on 2017/12/18.
 */
public class QRCodeGenUtils {

    public static String genQRCode(String text){
        String genUrl = "https://qr.api.cli.im/qr?data="+text+"&level=H&transparent=false&bgcolor=%23FFFFFF&forecolor=&blockpixel=12&marginblock=2&logourl=%2F%2Foss-cn-hangzhou.aliyuncs.com%2Fpublic-cli%2Ffree%2F94031ba010524ca29e2d1fd3791c2f2e1513603097.png&size=400&text=&logoshape=rect&fontsize=30&fontfamily=msyh.ttf&fontcolor=%23000000&incolor=&outcolor=&background=&qrcode_eyes=&wper=0&hper=0&lper=0&tper=0&eye_use_fore=1&qrpad=10&kid=cliim&key=cd01aa2cd83ac2b11fb45934894687d0";
        return genUrl;
    }

}
