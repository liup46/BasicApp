package com.basic.env

import android.content.Context
import com.basic.util.isDebug
import com.basic.util.nullEmpty
import com.meituan.android.walle.WalleChannelReader

/**
 * @author Peter Liu
 * @since 2023/3/19 11:34
 *
 */
object App {
    private var appContext: Context? = null
    private var httpConfig: HttpConfig? = null
    private var deviceConfig :DeviceConfig?= null
    private var userConfig:UserConfig? = null
    private var appChannel: String? = null

    fun init(context: Context) {
        appContext = context
    }

    fun getContext(): Context {
        return appContext!!
    }

    fun isDebug(): Boolean {
        return getContext().isDebug()
    }

    fun setUrlConfig(httpConfig: HttpConfig) {
        this.httpConfig = httpConfig
    }

    fun getUrlConfig(): HttpConfig {
        return httpConfig!!
    }

    fun setDeviceConfig(deviceConfig: DeviceConfig){
        this.deviceConfig = deviceConfig
    }

    fun getDeviceConfig():DeviceConfig{
        return deviceConfig!!
    }

    fun setUserConfig(userConfig: UserConfig){
        this.userConfig = userConfig
    }

    fun getUserConfig():UserConfig{
        return userConfig!!
    }

    fun getChannelId(): String? {
        if (appChannel == null) {
            //google  play的包需要写死channel  这里如果直接tag里面不为空，就直接使用Tag的值
            appChannel = if (!BuildConfig.CHANNEL.isNullOrBlank()) {
                BuildConfig.CHANNEL
            }else{
                val gw = getDeviceConfig().defaultChannel
                try {
                    WalleChannelReader.getChannel(getContext()).nullEmpty(gw)
                } catch (ignore: Throwable) {
                    gw
                }
            }
        }
        return appChannel
    }

}