package com.example.filepicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.PermissionUtils;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.functions.Consumer;

public class RxPermissionHelper {
    private static final String TAG = "RxPermissionHelper";
    private RxPermissions rxPermissions;
    private PermissionCallback callback;

    private Activity activity;

    private int count;
    private int num;
    private boolean hasForeverDenied;

    private Map<String, String> permissionNameMap = new HashMap<String, String>() {{
        put(Manifest.permission.WRITE_EXTERNAL_STORAGE, "读写储存权限");
        put(Manifest.permission.READ_EXTERNAL_STORAGE, "读取储存权限");
        put(Manifest.permission.ACCESS_COARSE_LOCATION, "定位权限");
        put(Manifest.permission.ACCESS_FINE_LOCATION, "定位权限");
        put(Manifest.permission.CAMERA, "相机权限");
    }};

    public RxPermissionHelper(@NonNull FragmentActivity activity) {
        rxPermissions = new RxPermissions(activity);
        this.activity = activity;
    }

    @SuppressLint("CheckResult")
    public void requestEach(@NonNull PermissionCallback callback, String... permissions) {
        this.callback = callback;
        num = permissions.length;
        count = 0;
        hasForeverDenied = false;

        // 检查是否全部授权
        // boolean allGranted = true;
        // for (String per : permissions) {
        //     if (!rxPermissions.isGranted(per)) {
        //         allGranted = false;
        //     }
        // }
        // if (!allGranted)
        request(permissions);
    }

    @SuppressLint("CheckResult")
    private void request(final String... permissions) {
        rxPermissions.requestEach(permissions)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        checkResult(permission, permissions);
                    }
                });
        // List<String> list = new ArrayList<>();
        // for (String per : permissions) {
        //     list.add(getPermissionName(per));
        // }
        // String name = List2StringUtil.combine(list);
        // String content = "需要获取" + name + "，否则部分功能不可用";
        //
        // SpannableString spannableString = new SpannableString(content);
        // int start = content.indexOf(name);
        // int end = start + name.length();
        // spannableString.setSpan(new ForegroundColorSpan(activity.getResources().getColor(R.color.colorAccent))
        //         , start,end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //
        // new MaterialDialog.Builder(activity)
        //         .title("权限申请")
        //         .content(spannableString)
        //         .positiveText("确认")
        //         .positiveColorRes(R.color.colorAccent)
        //         .onPositive(new MaterialDialog.SingleButtonCallback() {
        //             @Override
        //             public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        //                 rxPermissions.requestEach(permissions)
        //                         .subscribe(new Consumer<Permission>() {
        //                             @Override
        //                             public void accept(Permission permission) throws Exception {
        //                                 checkResult(permission, permissions);
        //                             }
        //                         });
        //             }
        //         })
        //         .cancelable(false)
        //         .show();
    }

    private void checkResult(Permission permission, String... permissions) {
        count++;
        String name = getPermissionName(permission.name);

        if (permission.granted) { // 授权
            // Log.i(TAG, "已获取权限: "+name);
            callback.granted(name);
        } else if (permission.shouldShowRequestPermissionRationale) { // 暂时拒绝
            // Log.w(TAG, "已拒绝权限: "+ name);
            callback.denied(name, false);
        } else { // 永久拒绝
            // Log.e(TAG, "已拒绝权限，并不再询问: "+ name);
            callback.denied(name, true);
            hasForeverDenied = true;
        }

        // 存在永久拒绝权限时需要提醒用户
        if (count == num && hasForeverDenied) {
            List<String> list = new ArrayList<>();
            for (String per : permissions) {
                list.add(getPermissionName(per));
            }
            String names = List2StringUtil.combine(list);
            String content = "部分权限被永久拒绝，请到系统设置授权" + names + "，否则部分功能不可用";

            SpannableString spannableString = new SpannableString(content);
            int start = content.indexOf(names);
            int end = start + names.length();
            spannableString.setSpan(new ForegroundColorSpan(activity.getResources().getColor(R.color.colorAccent))
                    , start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            new MaterialDialog.Builder(activity)
                    .title("权限申请")
                    .content(spannableString)
                    .positiveText("去授权")
                    .positiveColorRes(R.color.colorAccent)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            PermissionUtils.launchAppDetailsSettings();
                        }
                    })
                    .neutralText("取消")
                    .neutralColorRes(android.R.color.darker_gray)
                    .cancelable(false)
                    .show();
        }
    }

    public String getPermissionName(String androidPermission) {
        if (permissionNameMap.containsKey(androidPermission))
            return permissionNameMap.get(androidPermission);
        else
            return androidPermission;
    }

    public interface PermissionCallback {
        void granted(String permissionName);

        void denied(String permissionName, boolean forever);
    }
}
