package com.mob.sms2.network.service;

import com.mob.sms2.bean.BannerBean;
import com.mob.sms2.bean.ChannelChargeBean;
import com.mob.sms2.bean.CloudPermissionBean;
import com.mob.sms2.bean.HomeFuncBean;
import com.mob.sms2.bean.MsgResponse;
import com.mob.sms2.bean.OneDialBean;
import com.mob.sms2.bean.PhoneNumberBean;
import com.mob.sms2.bean.UpdateBean;
import com.mob.sms2.network.bean.AuthoLoginBean;
import com.mob.sms2.network.bean.BaseBean;
import com.mob.sms2.network.bean.BaseResponse;
import com.mob.sms2.network.bean.CallRecordBean;
import com.mob.sms2.network.bean.EnterpriseBean;
import com.mob.sms2.network.bean.FeedbackDetailBean;
import com.mob.sms2.network.bean.HistoryFeedBackBean;
import com.mob.sms2.network.bean.LoginBean;
import com.mob.sms2.network.bean.OnlineContactBean;
import com.mob.sms2.network.bean.OrderBean;
import com.mob.sms2.network.bean.OrderHistoryBean;
import com.mob.sms2.network.bean.QuestionBean;
import com.mob.sms2.network.bean.QuestionDetailBean;
import com.mob.sms2.network.bean.RuleBean;
import com.mob.sms2.network.bean.ShareBean;
import com.mob.sms2.network.bean.SmsRecordBean;
import com.mob.sms2.network.bean.UploadBean;
import com.mob.sms2.network.bean.UserInfoBean;
import com.mob.sms2.network.bean.VersionBean;
import com.mob.sms2.network.bean.VipBean;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface MyAPIService {
    // 手机号维护
    @GET("prod-api/restApi/tels/getTels")
    Observable<BaseResponse<PhoneNumberBean>> getPhoneNumbers();
    //发送验证码
    @POST("prod-api/restApi/appUser/sendSms")
    Observable<BaseBean> sendSms(@Query("username") String username);

    //注册
    @POST("prod-api/restApi/appUser/accountRegister")
    Observable<BaseBean> register(@Query("code") String code, @Query("confirmPwd") String confirmPwd, @Query("originate") String originate,
                                  @Query("password") String password, @Query("username") String username);

    //忘记密码
    @POST("prod-api/restApi/appUser/forgetPwd")
    Observable<BaseBean> forgetPwd(@Query("code") String code, @Query("confirmPwd") String confirmPwd,
                                  @Query("password") String password, @Query("username") String username);

    //登录
    @POST("prod-api/restApi/appUser/accountLogin")
    Observable<LoginBean> login(@Query("username") String username, @Query("password") String password);

    //绑定手机号
    @POST("prod-api/restApi/appUser/bingUsername")
    Observable<BaseBean> bindMobile(@Query("Authorization") String Authorization, @Query("username") String username, @Query("code") String code);

    //注销
    @POST("prod-api/restApi/appUser/delUser")
    Observable<BaseBean> delUser();

    //授权登录
    @POST("prod-api/restApi/appUser/authLogin")
    Observable<AuthoLoginBean> authLogin(@Query("avatar") String avatar, @Query("loginType") String loginType,
                                         @Query("nickName") String nickName, @Query("openid") String openid, @Query("originate") String originate);

    //获取常见问题
    @POST("prod-api/restApi/question/getQuestion")
    Observable<QuestionBean> getQuestion();

    //获取版本信息
    @POST("prod-api/restApi/version/getVersion")
    Observable<VersionBean> getVersion();

    //获取问题详情
    @GET("prod-api/restApi/question/detail/{id}")
    Observable<QuestionDetailBean> getQuestionDetail(@Path("id") int id);

    //相关协议
    @GET("prod-api/restApi/rule/getRule/{type}")
    Observable<RuleBean> getRule(@Path("type") int type);

    //获取升级版企业信息
    @POST("prod-api/restApi/enterprise/getInfo")
    Observable<EnterpriseBean> getEnterpriseInfo();

    //获取历史反馈
    @POST("prod-api/restApi/feedback/getHistory")
    Observable<HistoryFeedBackBean> getFeedback(@Query("pageNum") int pageNum, @Query("pageSize") int pageSize);

    //增加反馈
    @POST("prod-api/restApi/feedback/add")
    Observable<BaseBean> addFeedback(@Query("content") String content,
                                     @Query("image") String image, @Query("phone") String phone);

    //回复反馈
    @POST("prod-api/restApi/feedback/reply")
    Observable<BaseBean> replyFeedback(@Query("content") String content,
                                     @Query("feedbackId") int feedbackId, @Query("img") String img);

    //反馈详情
    @GET("prod-api/restApi/feedback/detail/{id}")
    Observable<FeedbackDetailBean> getDetailFeedback(@Path("id") int id);

    //获取分享信息
    @POST("prod-api/restApi/share/getShare")
    Observable<ShareBean> getShare();

    //获取全部套餐
    @POST("prod-api/restApi/level/getAll")
    Observable<VipBean> getVip();

    //添加联系人
    @POST("prod-api/restApi/address/addList")
    Observable<BaseBean> addContact(@Query("name") String name,
                                     @Query("tel") String tel);

    //获取联系人
    @POST("prod-api/restApi/address/getList")
    Observable<OnlineContactBean> getContacts();

    //删除联系人
    @POST("prod-api/restApi/address/delete")
    Observable<BaseBean> deleteContacts(@Query("ids") String ids);

    //获取短信记录
    @POST("prod-api/restApi/record/recordNote")
    Observable<SmsRecordBean> getSmsRecords(@Query("pageNum") int pageNum,
                                            @Query("pageSize") int pageSize);

    //获取拨号记录
    @POST("prod-api/restApi/record/recordDial")
    Observable<CallRecordBean> getCallRecords(@Query("pageNum") int pageNum,
                                              @Query("pageSize") int pageSize);

    //获取群拨记录
    @POST("prod-api/restApi/record/recordBatch")
    Observable<CallRecordBean> getPlCallRecords(@Query("pageNum") int pageNum,
                                         @Query("pageSize") int pageSize);

    //保存记录
    @POST("prod-api/restApi/record/saveRecord")
    Observable<BaseBean> saveRecord(@Query("allNum") int allNum,
                                             @Query("status") String status, @Query("successNum") int successNum,
                                             @Query("tels") String tels, @Query("type") int type);

    //保存短信记录
    @POST("prod-api/restApi/record/saveRecordNote")
    Observable<BaseBean> saveSmsRecord(@Query("allNum") int allNum,
                                    @Query("batchSend") String batchSend, @Query("content") String content,
                                    @Query("createTime") String createTime, @Query("oneSend") String oneSend,
                                       @Query("sendNum") int sendNum, @Query("status") String status,
                                       @Query("successNum") int successNum, @Query("tels") String tels,
                                       @Query("timing") String timing);

    //保存拨号记录
    @POST("prod-api/restApi/record/saveRecordDial")
    Observable<BaseBean> saveCallRecord(@Query("allNum") int allNum,
                                       @Query("createTime") String createTime, @Query("dialInterval") String dialInterval,
                                       @Query("dialNum") int dialNum, @Query("dialType") String dialType,
                                       @Query("onHang") String onHang, @Query("status") String status,
                                       @Query("successNum") int successNum, @Query("tels") String tels,
                                       @Query("timing") String timing, @Query("unHang") String unHang);

    //保存群拨记录
    @POST("prod-api/restApi/record/saveRecordBatch")
    Observable<BaseBean> savePlCallRecord(@Query("allNum") int allNum,
                                        @Query("cardSet") String cardSet, @Query("createTime") String createTime,
                                        @Query("intervalSet") String intervalSet, @Query("onHang") String onHang,
                                          @Query("status") String status, @Query("successNum") int successNum,
                                          @Query("tels") String tels, @Query("unHang") String unHang);

    //删除记录
    @POST("prod-api/restApi/record/deleteRecord")
    Observable<BaseBean> deleteRecord(@Query("ids") String ids);

    //删除短信记录
    @POST("prod-api/restApi/record/deleteRecordNote")
    Observable<BaseBean> deleteSmsRecord( @Query("ids") String ids);

    //删除拨号记录
    @POST("prod-api/restApi/record/deleteRecordDial")
    Observable<BaseBean> deleteCallRecord(@Query("ids") String ids);

    //删除群拨记录
    @POST("prod-api/restApi/record/deleteRecordBatch")
    Observable<BaseBean> deletePlCallRecord(@Query("ids") String ids);

    @Multipart
    @POST("prod-api/restApi/appUser/upload")
    Observable<UploadBean> upload(@PartMap Map<String, RequestBody> maps);

    //修改用户信息
    @POST("prod-api/restApi/appUser/updateUser")
    Observable<BaseBean> updateUser(@Query("avatar") String avatar,
                                    @Query("nickName") String nickName);

    //支付订单
    @POST("prod-api/restApi/pay/toPay")
    Observable<BaseBean> pay(@Query("orderId") int orderId,
                                    @Query("payType") String payType);

    //创建订单
    @POST("prod-api/restApi/order/createOrder")
    Observable<OrderBean> createOrder(@Query("relationId") int relationId);

    //获取用户信息
    @POST("prod-api/restApi/appUser/getUser")
    Observable<UserInfoBean> getUserInfo();

    //消费记录
    @POST("prod-api/restApi/order/history")
    Observable<OrderHistoryBean> getOrderHistory();

    //保存通话记录
    @POST("prod-api/restApi/call/saveLog")
    Observable<OrderHistoryBean> saveLog(@Query("dialTime") String dialTime,
                                         @Query("name") String name, @Query("tel") String tel);

    //获取通话记录
    @POST("prod-api/restApi/call/list")
    Observable<OrderHistoryBean> getLog(@Query("pageNum") int pageNum,
                                         @Query("pageSize") int pageSize);

    // 云拨号
    @POST("prod-api/restApi/cloud/useCloudDial")
    Observable<CloudPermissionBean> cloudDial();

    // 扣件云拨号分钟
    @POST("prod-api/restApi/cloud/reduceMinute")
    Observable<BaseBean> chargeCloudDial(@Query("minute")int minutes);

    @POST("prod-api/restApi/version/getVersion")
    Observable<BaseResponse<UpdateBean>> checkUpdate();

    //1启动页 2引导图 3广告位
    @POST("prod-api/restApi/advertising/getImg")
    Observable<BaseResponse<List<BannerBean>>> getImage(@Query("type")int type);

    @POST("prod-api/restApi/market/getAll")
    Observable<BaseResponse<Object>> getAllMarket();

    @POST("prod-api/restApi/share/getShare")
    Observable<BaseResponse<Object>> getShareInfo();

    // 是否收费:channelName
    @POST("prod-api/restApi/market/channelName")
    Observable<BaseResponse<ChannelChargeBean>> getMarketCharge(@Query("channelName")String channel);

    //获取首页功能列表 status用来显示隐藏功能 0隐藏 1显示
    @POST("prod-api/restApi/setting/getSetting")
    Observable<BaseResponse<List<HomeFuncBean>>> getHomeSetting();

    // 获取功能隐藏项目设置 status=1显示 0隐藏
    //1拨打电话 2批量拨打 3发送短信
    @POST("prod-api/restApi/setting/getHideSetting")
    Observable<BaseResponse<List<HomeFuncBean>>> getHiddenSetting(@Query("type")int type);

    // prod-api/restApi/setting/getOneDial
    @POST("prod-api/restApi/setting/getOneDial")
    Observable<BaseResponse<OneDialBean>> getOneDialTimes();

    //获取第三方接口信息,status用于显示隐藏=0隐藏 1显示
    @POST("prod-api/restApi/third/getThirdInfo")
    Observable<BaseResponse<HomeFuncBean>> getThirdInfo();

    // 消息
    @POST("prod-api/restApi/message/getMsg")
    Observable<BaseResponse<MsgResponse>> getNotifications();
}
