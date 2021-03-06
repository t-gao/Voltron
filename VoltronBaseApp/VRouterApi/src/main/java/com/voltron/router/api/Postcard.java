package com.voltron.router.api;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class Postcard {

    private Builder B;

    private Postcard(@NonNull Builder b) {
        B = b;
    }

    private boolean go() {
        return B.P.go();
    }

    PostcardInternal getPostcardInternal() {
        return B.P;
    }

    public Context getContext() {
        return B.P.context;
    }

    public String getScheme() {
        return B.P.getScheme();
    }

    public String getHost() {
        return B.P.getHost();
    }

    String getPath() {
        return B.P.getPath();
    }

    public String getRoute() {
        return B.P.getRoute();
    }

    public boolean isForResult() {
        return B.P.isForResult();
    }

    public int getRequestCode() {
        return B.P.getRequestCode();
    }

    public boolean hasExtra(String key) {
        return B.P.hasExtra(key);
    }

    /**
     * 如需修改该 Postcard (比如在拦截器内），需调用该方法。
     * 返回一个 {@link Builder}，对该 Builder 的修改就是对相应 Postcard 的修改。
     * @return 该 Postcard 的修改器 {@link Builder}
     */
    public Builder mutate() {
        return B;
    }

    /**
     * Builder for building or modifying a {@link Postcard} instance.
     */
    public static class Builder {

        private PostcardInternal P;

        public Builder(Context context) {
            P = new PostcardInternal(context);
        }

        /**
         * 设置以指定的 Fragment 来 startActivity。
         * <p>
         * NOTE:
         * 若调用了该方法，且参数不为 null，则会使用 {@link Fragment#startActivity(Intent)}
         * 或 {@link Fragment#startActivityForResult(Intent, int)}，
         * 而不会使用 Activity 的相应的方法
         *
         * @param fragment 指定的Fragment
         * @return this
         */
        public Builder startWithFragment(Fragment fragment) {
            P.setFragment(fragment);
            return this;
        }

        public Builder scheme(String scheme) {
            P.setScheme(scheme);
            return this;
        }

        public Builder host(String host) {
            P.setHost(host);
            return this;
        }

        public Builder path(String path) {
            P.setPath(path);
            return this;
        }

        /**
         * 直接指定完整的路由路径，其中包含 scheme、host、path，会覆盖三者相应单独指定的值。
         *
         * @param route 路由路径
         * @return this
         */
        public Builder route(String route) {
            P.setRoute(route);
            return this;
        }

        public Builder setIntentFlags(int flags) {
            P.setIntentFlags(flags);
            return this;
        }

        public Builder addIntentFlags(int flags) {
            P.addIntentFlags(flags);
            return this;
        }

        public Builder forResult(boolean forResult, int requestCode) {
            P.forResult(forResult, requestCode);
            return this;
        }

        public Builder forResult(int requestCode) {
            P.forResult(requestCode);
            return this;
        }

        public Builder intExtra(String key, int value) {
            P.myExtras().putInt(key, value);
            return this;
        }

        public Builder intArrayExtra(String key, int[] value) {
            P.myExtras().putIntArray(key, value);
            return this;
        }

        public Builder integerArrayListExtra(String key, ArrayList<Integer> value) {
            P.myExtras().putIntegerArrayList(key, value);
            return this;
        }

        public Builder stringExtra(String key, String value) {
            P.myExtras().putString(key, value);
            return this;
        }

        public Builder stringArrayExtra(String key, String[] value) {
            P.myExtras().putStringArray(key, value);
            return this;
        }

        public Builder stringArrayListExtra(String key, ArrayList<String> value) {
            P.myExtras().putStringArrayList(key, value);
            return this;
        }

        public Builder booleanExtra(String key, boolean value) {
            P.myExtras().putBoolean(key, value);
            return this;
        }

        public Builder booleanArrayExtra(String key, boolean[] value) {
            P.myExtras().putBooleanArray(key, value);
            return this;
        }

        public Builder charSequenceExtra(String key, CharSequence value) {
            P.myExtras().putCharSequence(key, value);
            return this;
        }

        public Builder charSequenceArrayExtra(String key, CharSequence[] value) {
            P.myExtras().putCharSequenceArray(key, value);
            return this;
        }

        public Builder charSequenceArrayListExtra(String key, ArrayList<CharSequence> value) {
            P.myExtras().putCharSequenceArrayList(key, value);
            return this;
        }

        public Builder charExtra(String key, char value) {
            P.myExtras().putChar(key, value);
            return this;
        }

        public Builder charArrayExtra(String key, char[] value) {
            P.myExtras().putCharArray(key, value);
            return this;
        }

        public Builder byteExtra(String key, byte value) {
            P.myExtras().putByte(key, value);
            return this;
        }

        public Builder byteArrayExtra(String key, byte[] value) {
            P.myExtras().putByteArray(key, value);
            return this;
        }

        public Builder longExtra(String key, long value) {
            P.myExtras().putLong(key, value);
            return this;
        }

        public Builder longArrayExtra(String key, long[] value) {
            P.myExtras().putLongArray(key, value);
            return this;
        }

        public Builder shortExtra(String key, short value) {
            P.myExtras().putShort(key, value);
            return this;
        }

        public Builder shortArrayExtra(String key, short[] value) {
            P.myExtras().putShortArray(key, value);
            return this;
        }

        public Builder doubleExtra(String key, double value) {
            P.myExtras().putDouble(key, value);
            return this;
        }

        public Builder doubleArrayExtra(String key, double[] value) {
            P.myExtras().putDoubleArray(key, value);
            return this;
        }

        public Builder floatExtra(String key, float value) {
            P.myExtras().putFloat(key, value);
            return this;
        }

        public Builder floatArrayExtra(String key, float[] value) {
            P.myExtras().putFloatArray(key, value);
            return this;
        }

        public Builder parcelableExtra(String key, Parcelable value) {
            P.myExtras().putParcelable(key, value);
            return this;
        }

        public Builder parcelableArrayExtra(String key, Parcelable[] value) {
            P.myExtras().putParcelableArray(key, value);
            return this;
        }

        public Builder parcelableArrayListExtra(String key, ArrayList<? extends Parcelable> value) {
            P.myExtras().putParcelableArrayList(key, value);
            return this;
        }

        public Builder serializableExtra(String key, Serializable value) {
            P.myExtras().putSerializable(key, value);
            return this;
        }

        /**
         * 会调用 {@link Bundle#putBundle(String, Bundle)}
         *
         * @param key   key
         * @param value value
         * @return this
         */
        public Builder bundleExtra(String key, @Nullable Bundle value) {
            P.myExtras().putBundle(key, value);
            return this;
        }

        /**
         * Inserts all the key-value mappings into this postcard's extras.
         * 将 bundle 内的全部 key-value 对添加进 extras.
         *
         * @param bundle a Bundle
         * @return this
         */
        public Builder putExtras(@Nullable Bundle bundle) {
            if (bundle != null) {
                P.myExtras().putAll(bundle);
            }
            return this;
        }

        public Builder putStringMapExtras(@Nullable Map<String, String> map) {
            if (map != null && !map.isEmpty()) {
                Bundle bundle = new Bundle();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    bundle.putString(entry.getKey(), entry.getValue());
                }
                P.myExtras().putAll(bundle);
            }
            return this;
        }

        /**
         * Overrides extras.
         * 直接设置 bundle 数据, 会覆盖现有 extras。
         */
        public Builder setExtra(@Nullable Bundle value) {
            P.setExtras(value);
            return this;
        }

        public Builder removeExtra(String key) {
            P.myExtras().remove(key);
            return this;
        }

        public Builder clearExtras() {
            P.clearExtras();
            return this;
        }

        public Builder addInterceptor(Interceptor interceptor) {
            P.addInterceptor(interceptor);
            return this;
        }

        public Builder removeInterceptor(Interceptor interceptor) {
            P.removeInterceptor(interceptor);
            return this;
        }

        public Builder clearInterceptors() {
            P.clearInterceptors();
            return this;
        }

        public Builder callback(@Nullable NavCallback callback) {
            P.callback(callback);
            return this;
        }

        /**
         * 跳转到指定页面
         *
         * @return 跳转结果： true 跳转成功；false 被拦截或跳转失败
         */
        public boolean go() {
            return build().go();
        }

        /**
         * 此处的conn全部由外部实现，不进行任何逻辑处理
         * flags与binsService设置的flags保持一致
         */
        public Builder bindService(@NonNull ServiceConnection serviceConnection, int flags) {
            P.conn = serviceConnection;
            P.flags = flags;
            P.bindService = true;
            return this;
        }

        @NonNull
        public Postcard build() {
            Postcard postcard = new Postcard(this);
            P.make(postcard);
            return postcard;
        }
    }
}
