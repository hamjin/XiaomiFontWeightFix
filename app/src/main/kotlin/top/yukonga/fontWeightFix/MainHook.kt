package top.yukonga.fontWeightFix

import android.graphics.Paint
import android.graphics.Typeface
import android.widget.TextView
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClassOrNull
import com.github.kyuubiran.ezxhelper.ClassUtils.loadFirstClassOrNull
import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createAfterHook
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createBeforeHook
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.ObjectHelper.Companion.objectHelper
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.lsposed.hiddenapibypass.HiddenApiBypass

class MainHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        EzXHelper.initHandleLoadPackage(lpparam)
        EzXHelper.setLogTag("XiaomiFontWeightFix")
        HiddenApiBypass.setHiddenApiExemptions("")

        when (lpparam.packageName) {
            "com.android.systemui" -> {
                try {
                    val systemProp: Class<*> = loadClassOrNull("android.os.SystemProperties")!!;
                    val osVersion = HiddenApiBypass.invoke(
                        systemProp,
                        HiddenApiBypass.newInstance(systemProp),
                        "getInt",
                        "ro.mi.os.version.code",
                        2
                    ) as Int

                    val miuiConfigsClass = loadClassOrNull("com.miui.utils.configs.MiuiConfigs")
                    val mobileTypeDrawableClass = loadFirstClassOrNull(
                        "com.miui.systemui.statusbar.views.MobileTypeDrawable",
                        "com.android.systemui.statusbar.views.MobileTypeDrawable"
                    )
                    val miuiNotificationHeaderViewClass =
                        loadClassOrNull("com.android.systemui.qs.MiuiNotificationHeaderView")

                    if (osVersion < 3) {
                        XposedHelpers.setStaticObjectField(
                            miuiConfigsClass, "sMiproTypeface", miFontTypeface(430)
                        )
                        XposedHelpers.setStaticObjectField(
                            mobileTypeDrawableClass, "sMiproTypeface", miFontTypeface(550)
                        )
                    } else {
                        XposedHelpers.setStaticObjectField(
                            miuiConfigsClass, "sMiproTypeface", miFontTypeface(700)
                        )
                        XposedHelpers.setStaticObjectField(
                            mobileTypeDrawableClass, "sMiproTypeface", miFontTypeface(660)
                        )
                    }

                    miuiNotificationHeaderViewClass?.methodFinder()
                        ?.filter { name.startsWith("updateResources") }?.first()
                        ?.createAfterHook {
                            it.thisObject.objectHelper().setObject("usingMiPro", true)
                        }

                    if (osVersion < 3) {
                        // OS2
                        miuiConfigsClass?.methodFinder()?.filter {
                            "setMiuiStatusBarTypeface" == name
                        }?.first()?.createBeforeHook {
                            @Suppress("UNCHECKED_CAST")
                            val textView: Array<TextView> =
                                it.args[0] as Array<TextView>
                            textView.forEach { tv ->
                                tv.typeface = miFontTypeface(430)
                            }
                            it.result = null
                        }
                    } else {
                        // OS3
                        miuiConfigsClass?.methodFinder()?.filter {
                            "applyStatusBarTypeface" == name
                        }?.first()?.createBeforeHook {
                            @Suppress("UNCHECKED_CAST")
                            val textView: Array<TextView> =
                                it.args[1] as Array<TextView>
                            textView.forEach { tv ->
                                tv.typeface = miFontTypeface(630)
                            }
                            it.result = null
                        }
                    }

                    mobileTypeDrawableClass?.methodFinder()
                        ?.filterByName("setMiuiStatusBarTypeface")?.first()?.createBeforeHook {
                            @Suppress("UNCHECKED_CAST") val paint = it.args[0] as Array<Paint>
                            paint.forEach { p ->
                                if (osVersion < 3)
                                    p.typeface = miFontTypeface(550)
                                else
                                    p.typeface = miFontTypeface(750)
                            }
                            it.result = null
                        }
                } catch (t: Throwable) {
                    Log.ex(t)
                }
            }

            else -> return
        }
    }
}

fun miFontTypeface(wght: Int): Typeface =
    Typeface.Builder("/system/fonts/MiSansVF.ttf").setFontVariationSettings("'wght' $wght")
        .build()
