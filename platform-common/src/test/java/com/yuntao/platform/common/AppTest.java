package com.yuntao.platform.common;

import com.yuntao.platform.common.support.QQGzh.QQAccessToken;
import com.yuntao.platform.common.support.QQGzh.QQGzhUtils;
import com.yuntao.platform.common.support.QQGzh.QQUserInfo;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest
        extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        assertTrue(true);
    }

//    @org.junit.Test
    public void test01(){
        QQAccessToken d = QQGzhUtils.getAccessToken("095BC1BE0C03978D7C7C8161C629B56E","http://h5.doublefit.cn/invite/shareCallbackByQQ/295/163/FP88");
        d = QQGzhUtils.getOpenId(d.getAccessToken());//C2F152AE50F6DFC03968CC7F2E3DDE15
//        d = QQGzhUtils.getOpenId("C2F152AE50F6DFC03968CC7F2E3DDE15");//95BC15F21C048DE94E9D7E7F4CC5E268
//        QQAccessToken d = new QQAccessToken();
//                d.setOpenid("95BC15F21C048DE94E9D7E7F4CC5E268");
//        d.setAccessToken("C2F152AE50F6DFC03968CC7F2E3DDE15");
        QQUserInfo a = QQGzhUtils.getUserInfo(d.getAccessToken(),d.getOpenid());
    }
}
