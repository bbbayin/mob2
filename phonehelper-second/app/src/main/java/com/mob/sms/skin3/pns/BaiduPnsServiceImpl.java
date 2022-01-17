package com.mob.sms.skin3.pns;

import com.alibaba.fastjson.JSONObject;
import com.mob.sms.skin3.utils.Constants;
import com.mob.sms.skin3.utils.SPUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * 百度 号码隐私保护服务 PNS
 * https://cloud.baidu.com/doc/PNS/s/Dk1t793rx
 *
 * @author xuefeng
 */
public class BaiduPnsServiceImpl {

    private static final String ACCESS_KEY_ID = "fe205c344a4d4a2cae7fb8bb10dec20d"; // 用户的Access Key ID
    private static final String SECRET_ACCESS_KEY = "1af0a6a68be34c5985b456123721a4b1"; // 用户的Secret Access Key

    /**
     * Ax模式，绑定虚拟号码
     *
     * @param telA 需要绑定的电话号码
     * @throws Exception
     */
    public String bindingAxb(String telA, String telB) {
//		String[] areacodes = new String[]{"10", "21", "20", "765", "22", "28", "571", "25", "512", "566"};

        JSONObject data = new JSONObject();
        data.put("telA", telA); //A号码
        data.put("telB", telB); //B号码
        String xNumber = SPUtils.getString(Constants.SECRET_NUMBER, "");
        data.put("telX", xNumber); //X号码
//		Random random = new Random();
//		int randomValue = random.nextInt(10);
//    	data.put("areaCode", areacodes[randomValue]); //需要X号码所属区号
        data.put("record", 0); //是否录音，1：录音；0：不录音
        data.put("expiration", 600); //绑定失效时间（秒）
        //data.put("customer", ""); //随传数据

        String utcTime = getUTCTime(); //获取当前utc时间
        String authStringPrefix = String.format("bce-auth-v1/%s/%s/%s", ACCESS_KEY_ID, utcTime, data.getString("expiration"));
        // \n 为换行符，host前面有两个\n\n 因为没有把请求参数放进去但位置要保留，所以有两个
        String canonicalRequest = "POST\n/cloud/api/v1/axb/binding\n\nhost:pns.baidubce.com";

        try {
            String signingKey = HMAC_SHA256_HEX(SECRET_ACCESS_KEY, authStringPrefix); //加密
            String signature = HMAC_SHA256_HEX(signingKey, canonicalRequest); //在加密，得到signature
            //生成认证字符串  https://cloud.baidu.com/doc/Reference/s/njwvz1yfu
            String authorization = String.format("%s/host/%s", authStringPrefix, signature); //加密认证字符串
            //发送绑定虚拟号码请求
            return HttpUtil.postBaiduPNS("https://pns.baidubce.com/cloud/api/v1/axb/binding", data.toString(), authorization);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }


    public void post(String bindId) {
        String utcTime = getUTCTime(); //获取当前utc时间
        String authStringPrefix = String.format("bce-auth-v1/%s/%s/%s", ACCESS_KEY_ID, utcTime, "60");
        // \n 为换行符，host前面有两个\n\n 因为没有把请求参数放进去但位置要保留，所以有两个
        String canonicalRequest = String.format("POST\n/cloud/api/v1/axb/unbinding/%s\n\nhost:pns.baidubce.com", bindId);


        String url = "https://pns.baidubce.com/cloud/api/v1/axb/unbinding/" + bindId;
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), "");
        OkHttpClient okHttpClient = new OkHttpClient();

        try {
			String signingKey = HMAC_SHA256_HEX(SECRET_ACCESS_KEY, authStringPrefix); //加密
			String signature = HMAC_SHA256_HEX(signingKey, canonicalRequest); //在加密，得到signature
			//生成认证字符串  https://cloud.baidu.com/doc/Reference/s/njwvz1yfu
			String authorization = String.format("%s/host/%s", authStringPrefix, signature); //加密认证字符串
//connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
//		connection.setRequestProperty("Authorization", authorization);

			Request request = new Request.Builder()
					.url(url)
					.header("Authorization", authorization)
					.post(requestBody)
					.build();

            Response execute = okHttpClient.newCall(request).execute();
            System.out.println("解绑结果：" + execute.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 解除虚拟号绑定
     *
     * @param bindId 绑定id
     */
    public String unbinding(String bindId) {

        String utcTime = getUTCTime(); //获取当前utc时间
        String authStringPrefix = String.format("bce-auth-v1/%s/%s/%s", ACCESS_KEY_ID, utcTime, "60");
        // \n 为换行符，host前面有两个\n\n 因为没有把请求参数放进去但位置要保留，所以有两个
        String canonicalRequest = String.format("POST\n/cloud/api/v1/axb/unbinding/%s\n\nhost:pns.baidubce.com", bindId);

        try {
            String signingKey = HMAC_SHA256_HEX(SECRET_ACCESS_KEY, authStringPrefix); //加密
            String signature = HMAC_SHA256_HEX(signingKey, canonicalRequest); //在加密，得到signature
            //生成认证字符串  https://cloud.baidu.com/doc/Reference/s/njwvz1yfu
            String authorization = String.format("%s/host/%s", authStringPrefix, signature); //加密认证字符串
            //发送绑定虚拟号码请求
            ///cloud/api/v{version}/axb/unbinding/{bindId}
            String url = "https://pns.baidubce.com/cloud/api/v1/axb/unbinding/" + bindId;
            return HttpUtil.postBaiduPNS(url, "", authorization);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }


    /**
     * 获取UTC时间，类型为字符串，格式为"yyyy-MM-ddThh:mm:ssZ"
     *
     * @return
     */
    public static String getUTCTime() {

        Calendar cal = Calendar.getInstance(); // 1、取得本地时间：
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET); // 2、取得时间偏移量：
        int dstOffset = cal.get(Calendar.DST_OFFSET); // 3、取得夏令时差：
        // 4、从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));

        StringBuffer utcSb = new StringBuffer();
        utcSb.append(cal.get(Calendar.YEAR)).append("-");
        utcSb.append(String.format("%02d", cal.get(Calendar.MONTH) + 1)).append("-");
        utcSb.append(String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))).append("T");

        utcSb.append(String.format("%02d", cal.get(Calendar.HOUR_OF_DAY))).append(":");
        utcSb.append(String.format("%02d", cal.get(Calendar.MINUTE))).append(":");
        utcSb.append(String.format("%02d", cal.get(Calendar.SECOND))).append("Z");

        return utcSb.toString();
    }


    /**
     * 微信签名算法 HMAC-SHA256
     * https://www.jianshu.com/p/f4be613acf18
     */
    private static String HMAC_SHA256_HEX(String secret, String message) throws Exception {

        //------------------------sha256_HMAC加密 -----------------------
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] bytes = sha256_HMAC.doFinal(message.getBytes()); //加密后的字节数组

        String stmp;
        //------------------ 将加密后的字节数组转换成字符串 -------------------
        StringBuilder hs = new StringBuilder();
        for (int n = 0; n < bytes.length; n++) {
            stmp = Integer.toHexString(bytes[n] & 0XFF);
            if (stmp.length() == 1) hs.append('0');
            hs.append(stmp);
        }

        return hs.toString().toLowerCase();

    }


}
