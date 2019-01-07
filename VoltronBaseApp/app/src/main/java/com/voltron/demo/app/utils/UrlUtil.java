package com.voltron.demo.app.utils;

import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;

public class UrlUtil {

    private UrlUtil() {
        throw new RuntimeException("can't be instance");// avoid instance and reflect
    }

    /**
     * 检查是否是有效的url，eg：myscheme://m.test.com/modulea/demoa
     * 如果不包含后面的path，则无法实现跳转
     */
    public static boolean checkIsLegalDeepLinkPath(String route) {
        try {
            Uri uri = Uri.parse(route);
            return !TextUtils.isEmpty(uri.getPath());
        } catch (Exception e) {
            return false;
        }
    }

    // 获取deepLink Url中需要跳转的path
    public static String getRouterPath(String route) {
        try {
            Uri uri = Uri.parse(route);
            return TextUtils.isEmpty(uri.getPath()) ? "" : uri.getPath();
        } catch (Exception e) {
            return "";
        }
    }

    //解析url中的键值对
    public static HashMap<String, String> URLRequest(String URL) {
        HashMap<String, String> mapRequest = new HashMap<String, String>();
        String[] arrSplit;

        String strUrlParam = TruncateUrlPage(URL);
        if (strUrlParam == null) {
            return mapRequest;
        }
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");
            if (arrSplitEqual.length > 1) {
                //去除"#fragment"
                if (arrSplitEqual[1].contains("#fragment")) {
                    arrSplitEqual[1] = arrSplitEqual[1].substring(0, arrSplitEqual[1].indexOf("#fragment"));
                }
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
            } else {
                if (arrSplitEqual[0] != "") {
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }

    private static String TruncateUrlPage(String strUrl) {

        if (TextUtils.isEmpty(strUrl)) {
            return "";
        }
        String strAllParam = "";
        String arrSplit[] = null;
        strUrl = strUrl.trim();

        arrSplit = strUrl.split("[?]");
        if (strUrl.length() > 1) {
            if (arrSplit.length > 1) {
                if (arrSplit[1] != null) {
                    strAllParam = arrSplit[1];
                }
            }
        }
        return strAllParam;
    }
}
