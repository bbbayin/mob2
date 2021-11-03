package com.mob.sms3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

public class ShareManager {

    private static ShareManager shareManager;

    public static ShareManager getInstance() {
        if (shareManager == null) {
            shareManager = new ShareManager();
        }
        return shareManager;
    }

    public void shareToQQ(Activity activity, String title, String thumb, String content, String url) {
        UMWeb web = new UMWeb(url);
        web.setTitle(title);//标题
        web.setThumb(new UMImage(activity, thumb));  //缩略图
        web.setDescription(content);//描述
        new ShareAction(activity).setPlatform(SHARE_MEDIA.QQ)
                .withMedia(web)
                .share();
    }

    public void shareToQQZero(Activity activity, String title, String thumb, String content, String url) {
        UMWeb web = new UMWeb(url);
        web.setTitle(title);//标题
        web.setThumb(new UMImage(activity, thumb));  //缩略图
        web.setDescription(content);//描述
        new ShareAction(activity).setPlatform(SHARE_MEDIA.QZONE)
                .withMedia(web)
                .share();
    }

    public void shareToWeChat(Activity activity, String title, String thumb, String content, String url) {
        UMWeb web = new UMWeb(url);
        web.setTitle(title);//标题
        web.setThumb(new UMImage(activity, thumb));  //缩略图
        web.setDescription(content);//描述
        new ShareAction(activity).setPlatform(SHARE_MEDIA.WEIXIN)
                .withMedia(web)
                .share();
    }

    public void shareToWeChatCircle(Activity activity, String title, String thumb, String content, String url) {
        UMWeb web = new UMWeb(url);
        web.setTitle(title);
        web.setThumb(new UMImage(activity, thumb));
        web.setDescription(content);
        new ShareAction(activity).setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)
                .withMedia(web)
                .share();
    }



    public void onActivityResult(Context context, int requestCode, int resultCode, Intent data) {
        UMShareAPI.get(context).onActivityResult(requestCode, resultCode, data);
    }
}
