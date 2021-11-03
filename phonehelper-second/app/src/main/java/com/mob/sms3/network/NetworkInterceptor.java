package com.mob.sms3.network;

import com.mob.sms3.utils.SPConstant;
import com.mob.sms3.utils.SPUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String token = SPUtils.getString(SPConstant.SP_USER_TOKEN, "");
        Request newReq = request.newBuilder()
                .addHeader("Authorization", token)
                .build();
        return chain.proceed(newReq);
    }
}
