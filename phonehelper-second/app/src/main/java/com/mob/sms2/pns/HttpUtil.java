package com.mob.sms2.pns;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * http 工具类
 */
public class HttpUtil {
	
	public static String UTF_8 = "UTF-8";
    
    /**
     * POST methed
     * @param url
     * @param params
     * @param authorization
     * @return
     * @throws IOException  
     */
    public static String postBaiduPNS(String url, String params, String authorization) throws IOException {
        
    	HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		//connection.setRequestProperty("x-bce-date", xbcedata);
		connection.setRequestProperty("Authorization", authorization);
		
		connection.setUseCaches(false);
		connection.setDoOutput(true);
		connection.setDoInput(true);

		// 得到请求的输出流对象
		DataOutputStream out = new DataOutputStream(connection.getOutputStream());
		out.write(params.getBytes(UTF_8));
		out.flush();
		out.close();

		// 建立实际的连接
		connection.connect();
		
//		 // 获取所有响应头字段
//        Map<String, List<String>> headers = connection.getHeaderFields();
//        // 遍历所有的响应头字段
//        for (String key : headers.keySet()) {
//            System.err.println(key + "--->" + headers.get(key));
//        }

		String getLine, result = "";
		// 定义 BufferedReader输入流来读取URL的响应
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), UTF_8));
		while ((getLine = in.readLine()) != null) {
		    result += getLine;
		}
		in.close();
		
		return result;
    }
    
    
    
}
