package com.yuntao.platform.common.constant;

/**
 * Created by shengshan.tang on 2015/11/24 at 16:45
 */
public interface SystemConstant {

    String TABLE_POSTFIX = "tablePostfix";

    String AUTH_USER = "auth_user";
    String AUTH_PWD = "auth_pwd";
    String GEN_DOC = "genDoc";
    String USER_TOKEN = "sid";
    String USER_BOS_MENUS = "userBosMenus";
    String USER_BOS_URLS = "userBosUrls";
    String USER_BOS_NOTCHECK_URLS = "userBosNotCheckUrls";

    interface ResponseLevel{
        String INFO = "info";
        String WARN = "warn";
        String ERROR = "error";
    }

    interface ResponseType{

        String NORMAL = "normal";
        String OFF_LINE = "off_line";
    }
    interface ResponseBizType{
        String NORMAL = "normal";
    }

    interface ResponseCode{
        String NORMAL = "00";
        String NOT_LOGIN = "01";
        String NOT_AUTHORITY = "02";
        String SYSTEM_ERROR = "03";
        String NOT_BIND = "04";   //第三方登录的,手机账号未绑定
    }

    interface ExceptionCode{
        int NORMAL = 0;
        int REMOTE_TIME_OUT = 1;  //连接超时
    }


    interface  TuiSong{

        String dfGetuiTest = "df_getui_test";
        String dfGetuiProd = "df_getui_prod";

    }


}
