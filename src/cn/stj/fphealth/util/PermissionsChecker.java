package cn.stj.fphealth.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import cn.stj.fphealth.app.FPHealthApplication;

/**
 * 检查权限的工具类
 */
public class PermissionsChecker {
    
    // 判断权限集合
    public static boolean lacksPermissions(String... permissions) {
        for (String permission : permissions) {
            if (lacksPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    // 判断是否缺少权限
    public static boolean lacksPermission(String permission) {
        /*LogUtil.i("debug", "=@@@@@======PermissionsChecker=====lacksPermission==========@@@@===permission:" + permission + 
                "====" + ContextCompat.checkSelfPermission(FPHealthApplication.getInstance().getApplicationContext(), permission)) ;*/
        return ContextCompat.checkSelfPermission(FPHealthApplication.getInstance().getApplicationContext(), permission) ==
                PackageManager.PERMISSION_DENIED;
    }
}
