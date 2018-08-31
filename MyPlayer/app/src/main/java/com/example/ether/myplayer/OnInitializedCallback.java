package com.example.ether.myplayer;

/**
 * @author yetote QQ:503779938
 * @name MyPlayer
 * @class name：com.example.ether.myplayer
 * @class 初始化接口，应该是进行网络连接用的，rtmp拉流用的
 * @time 2018/8/31 10:35
 * @change
 * @chang time
 * @class describe
 */
public interface OnInitializedCallback {
    /**
     * 连接状态
     */
    enum OnInitialStatus {
        /**
         * 连接成功
         */
        CONNECT_SUCESS,
        /**
         * 连接失败
         */
        CONNECT_FAILED,
        /**
         * 连接取消
         */
        CLINET_CANCEL
    }

    ;

    public void onInitialized(OnInitialStatus onInitialStatus);
}