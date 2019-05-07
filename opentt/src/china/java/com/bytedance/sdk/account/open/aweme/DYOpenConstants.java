package com.bytedance.sdk.account.open.aweme;

public class DYOpenConstants {

    public static final int OPEN_SDK_VERSION = 0;
    public static final int SUPPORT_DY_VERSION = 0;

    public static final String AWEME_EXTRA_MEDIA_MESSAGE_IMAGE_PATH = "AWEME_EXTRA_IMAGE_MESSAGE_PATH";
    public static final String AWEME_EXTRA_MEDIA_MESSAGE_VIDEO_PATH = "AWEME_EXTRA_VIDEO_MESSAGE_PATH";

    /**
     * Bundle 动作类型
     */
    public interface ModeType {
        /**
         * auth Request
         */
        int SEND_AUTH_REQUEST = 1;
        /**
         * auth Response
         */
        int SEND_AUTH_RESPONSE = 2;

        /**
         * share
         */
        int SHARE_CONTENT_TO_DY = 3;

        /**
         * share response
         */
        int SHARE_CONTENT_TO_DY_RESP = 4;
    }

    /**
     * 请求参数 key
     */
    public interface Params {
        String STATE = "_aweme_open_sdk_params_state";
        String CLIENT_KEY = "_aweme_open_sdk_params_client_key";
        String CALLER_PKG = "_aweme_open_sdk_params_caller_package";
        String CALLER_SDK_VERSION = "_aweme_open_sdk_params_caller_sdk_version";
        String CALLER_LOCAL_ENTRY = "_aweme_open_sdk_params_caller_local_entry";
        String SHARE_TARGET_SCENE = "_aweme_open_sdk_params_target_scene";

        /**
         * extra 内容
         */
        String EXTRA = "_aweme_open_sdk_params_extra";
        String TYPE = "_aweme_open_sdk_params_type";

        String ERROR_CODE = "_aweme_open_sdk_params_error_code";
        String ERROR_MSG = "_aweme_open_sdk_params_error_msg";
    }

    public interface TargetSceneType {
        int SHARE_DEFAULT_TYPE = 0;
    }

    public interface ErrorCode {
        int ERR_OK = 0;      // 成功
        int ERR_FAILED_COMMON = -1; // 通用错误类型
        int ERR_USER_CANCEL = -2; // 用户手动取消
        int ERR_SEND_FAIL = -3; // 发送失败
        int ERR_AUTH_DENIED = -4; // 权限错误
        int ERR_UNSUPPORT = -5; // 不支持
    }
}