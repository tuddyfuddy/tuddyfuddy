package com.survivalcoding.a510

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, "a167264ee18a4fc52fd2238b8c445a39")
    }
}