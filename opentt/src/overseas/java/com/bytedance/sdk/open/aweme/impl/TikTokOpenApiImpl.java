package com.bytedance.sdk.open.aweme.impl;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.bytedance.sdk.account.bdopen.api.BDOpenApi;
import com.bytedance.sdk.account.common.api.BDApiEventHandler;
import com.bytedance.sdk.account.common.constants.BDOpenConstants;
import com.bytedance.sdk.account.common.model.BaseResp;
import com.bytedance.sdk.account.common.model.SendAuth;
import com.bytedance.sdk.open.aweme.DYOpenConstants;
import com.bytedance.sdk.open.aweme.IAPPCheckHelper;
import com.bytedance.sdk.open.aweme.api.TiktokOpenApi;
import com.bytedance.sdk.open.aweme.authorize.Authorization;
import com.bytedance.sdk.open.aweme.share.Share;
import com.bytedance.sdk.open.aweme.share.ShareImpl;

/**
 * Tiktok授权实现类
 *
 * @author changlei@bytedance.com
 */

class TikTokOpenApiImpl implements TiktokOpenApi {

    private BDOpenApi bdOpenApi;

    private final IAPPCheckHelper[] mAuthcheckApis;
    private final IAPPCheckHelper[] mSharecheckApis;

    private ShareImpl shareImpl;

    static final int API_TYPE_LOGIN = 0;
    static final int API_TYPE_SHARE = 1;

    static final String LOCAL_ENTRY_ACTIVITY = "bdopen.BdEntryActivity"; // 请求授权的结果回调Activity入口
    static final String REMOTE_SHARE_ACTIVITY = "share.SystemShareActivity"; // 分享的Activity入口

    TikTokOpenApiImpl(Context context, BDOpenApi bdOpenApi, ShareImpl shareImpl) {
        this.bdOpenApi = bdOpenApi;
        this.shareImpl = shareImpl;
        mAuthcheckApis = new IAPPCheckHelper[] {
                new MusicallyCheckHelperImpl(this.bdOpenApi),
                new TiktokCheckHelperImpl(this.bdOpenApi)
        };

        mSharecheckApis = new IAPPCheckHelper[] {
                new MusicallyCheckHelperImpl(this.bdOpenApi),
                new TiktokCheckHelperImpl(this.bdOpenApi)
        };
    }

    @Override
    public boolean handleIntent(Intent intent, BDApiEventHandler eventHandler) {
        if (eventHandler == null) {
            return false;
        }
        if (intent == null) {
            eventHandler.onErrorIntent(intent);
            return false;
        }
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            eventHandler.onErrorIntent(intent);
            return false;
        }

        int type = bundle.getInt(BDOpenConstants.Params.TYPE) == 0 ? bundle.getInt(DYOpenConstants.Params.TYPE) : 0;
        return distributionIntent(type, intent, eventHandler);
    }

    @Override public boolean isAppSupportAuthorization(int targetApp) {
        if (targetApp == DYOpenConstants.TARGET_APP.AWEME) {
            return new AwemeCheckHelperImpl(bdOpenApi).isAppSupportAuthorization();
        } else {
            return getSupportApiAppInfo(API_TYPE_LOGIN) != null;
        }
    }

    @Override
    public boolean isAppSupportShare(int targetApp) {
        if (targetApp == DYOpenConstants.TARGET_APP.AWEME) {
            return new AwemeCheckHelperImpl(bdOpenApi).isAppSupportShare();
        } else {
            return getSupportApiAppInfo(API_TYPE_SHARE) != null;
        }
    }

    /**
     * 应部分厂商需求打开此api. 不太建议使用
     *
     * 就算安装了，版本不支持，功能一样不可以使用，可以直接用功能判断接口;
     * @param targetApp
     * @return
     */
    @Override public boolean isAppInstalled(int targetApp) {
        if (targetApp == DYOpenConstants.TARGET_APP.AWEME) {
            return new AwemeCheckHelperImpl(bdOpenApi).isAppInstalled();
        } else {
            for (IAPPCheckHelper checkapi : mAuthcheckApis) {
                if (checkapi.isAppInstalled()) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean distributionIntent(int type, Intent intent, BDApiEventHandler eventHandler) {
        switch (type) {
            case BDOpenConstants.ModeType.SEND_AUTH_REQUEST:
            case BDOpenConstants.ModeType.SEND_AUTH_RESPONSE:
                return bdOpenApi.handleIntent(intent, eventHandler);
            case DYOpenConstants.ModeType.SHARE_CONTENT_TO_DY:
            case DYOpenConstants.ModeType.SHARE_CONTENT_TO_DY_RESP:
                return shareImpl.handleShareIntent(intent, eventHandler);
            default:
                return bdOpenApi.handleIntent(intent, eventHandler);
        }
    }

    @Override
    public boolean sendAuthLogin(Authorization.Request request) {
        IAPPCheckHelper appHasInstalled;
        if (request.targetApp == DYOpenConstants.TARGET_APP.AWEME) {
            appHasInstalled = new AwemeCheckHelperImpl(bdOpenApi);
            if (!appHasInstalled.isAppSupportAuthorization()) {
                // 这个时候抖音没安装所以要走web授权
                appHasInstalled = null;
            }
        } else if (request.targetApp == DYOpenConstants.TARGET_APP.TIKTOK) {
            appHasInstalled = getSupportApiAppInfo(API_TYPE_LOGIN);
        } else {
            throw new IllegalArgumentException("We only support AWEME And TIKTOK for authorization.");
        }
        if (appHasInstalled != null && appHasInstalled.sendRemoteRequest(LOCAL_ENTRY_ACTIVITY, request)) {
            return true;
        } else {
            return sendInnerWebAuthRequest(request);
        }
    }

    @Override
    public boolean share(Share.Request request) {
        if (request == null) {
            return false;
        }

        // 适配抖音
        if (request.mTargetApp == DYOpenConstants.TARGET_APP.AWEME) {
            AwemeCheckHelperImpl checkHelper = new AwemeCheckHelperImpl(bdOpenApi);
            if (bdOpenApi != null && checkHelper.isAppSupportShare()) {
                return shareImpl.share(LOCAL_ENTRY_ACTIVITY, checkHelper.getPackageName(), REMOTE_SHARE_ACTIVITY, request);
            }
        } else {
            // MT需要判断用户安装了哪个，并且哪个支持分享功能
            if (isAppSupportShare(request.mTargetApp)) {
                String remotePackage = getSupportApiAppInfo(API_TYPE_SHARE).getPackageName();// 授权方包名
                return shareImpl.share(LOCAL_ENTRY_ACTIVITY, remotePackage, REMOTE_SHARE_ACTIVITY, request);
            }
        }

        return false;
    }

    @Override
    public boolean handleShareIntent(Intent intent, BDApiEventHandler eventHandler) {
        return shareImpl.handleShareIntent(intent, eventHandler);
    }

    @Override
    public boolean sendInnerWebAuthRequest(Authorization.Request request) {
        if (request.targetApp == DYOpenConstants.TARGET_APP.TIKTOK) {
            return bdOpenApi.sendInnerWebAuthRequest(TikTokWebAuthorizeActivity.class, request);
        } else if (request.targetApp == DYOpenConstants.TARGET_APP.AWEME) {
            return bdOpenApi.sendInnerWebAuthRequest(AwemeWebAuthorizeActivity.class, request);
        } else {
            throw new IllegalArgumentException("We only support AWEME And TIKTOK for authorization.");
        }
    }

    @Override
    public boolean preloadWebAuth(Authorization.Request request) {
        if (request.targetApp == DYOpenConstants.TARGET_APP.TIKTOK) {
            return bdOpenApi.preloadWebAuth(request, TikTokWebAuthorizeActivity.AUTH_HOST, TikTokWebAuthorizeActivity.AUTH_PATH,
                    TikTokWebAuthorizeActivity.DOMAIN);
        } else {
            return bdOpenApi.preloadWebAuth(request, AwemeWebAuthorizeActivity.AUTH_HOST, AwemeWebAuthorizeActivity.AUTH_PATH,
                    AwemeWebAuthorizeActivity.DOMAIN);
        }
    }

    @Override
    public boolean sendInnerResponse(SendAuth.Request req, BaseResp resp) {
        return bdOpenApi.sendInnerResponse(LOCAL_ENTRY_ACTIVITY, req, resp);
    }

    @Nullable
    private IAPPCheckHelper getSupportApiAppInfo(int type) {

        switch (type) {
            case API_TYPE_LOGIN:
                for (IAPPCheckHelper checkapi : mAuthcheckApis) {
                    if (checkapi.isAppSupportAuthorization()) {
                        return checkapi;
                    }
                }
                break;
            case API_TYPE_SHARE:
                for (IAPPCheckHelper checkapi : mSharecheckApis) {
                    if (checkapi.isAppSupportShare()) {
                        return checkapi;
                    }
                }
                break;
        }

        return null;
    }
}