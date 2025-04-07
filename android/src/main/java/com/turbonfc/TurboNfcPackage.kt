package com.turbonfc

import android.view.View
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ReactShadowNode
import com.facebook.react.uimanager.ViewManager

class TurboNfcPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): 
        List<NativeModule> {
        return listOf(TurboNfcModule(reactContext))
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): 
        List<ViewManager<View, ReactShadowNode<*>>> {
        return emptyList()
    }
}