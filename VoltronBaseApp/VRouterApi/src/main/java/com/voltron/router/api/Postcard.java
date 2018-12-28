package com.voltron.router.api;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.io.Serializable;
import java.util.ArrayList;

public class Postcard {
    Context context;
    Fragment fragment;

    String group;
    String path;
    Bundle extras;
    int intentFlags;
    boolean forResult;
    int requestCode = -1;

    Postcard(Context context, Fragment fragment, String path, Bundle extras, int intentFlags,
             boolean forResult, int requestCode) {

        this.context = context;
        this.fragment = fragment;
        this.path = path;
        this.extras = extras;
        this.intentFlags = intentFlags;
        this.forResult = forResult;
        this.requestCode = requestCode;
    }

    Postcard(Context context) {
        this.context = context;
    }

    Context getContext() {
        return context;
    }

    Fragment getFragment() {
        return fragment;
    }

    String getGroup() {
        return group;
    }

    String getPath() {
        return path;
    }

    Bundle getExtras() {
        return extras;
    }

    int getIntentFlags() {
        return intentFlags;
    }

    public boolean isForResult() {
        return forResult;
    }

    public int getRequestCode() {
        return requestCode;
    }


    @NonNull
    private Bundle myExtras() {
        Bundle ext = this.extras;
        if (ext == null) {
            ext = new Bundle();
            this.extras = ext;
        }
        return ext;
    }

    /**
     * 跳转到指定页面
     * @return 跳转结果
     */
    boolean go() {
        return VRouterInternal.go(this);
    }

    public static class Builder {

        private Postcard P;

        public Builder(Context context) {
            P = new Postcard(context);
        }

        /**
         * 设置以指定的 Fragment 来 startActivity。
         *
         * NOTE:
         * 若调用了该方法，且参数不为 null，则会使用 {@link Fragment#startActivity(Intent)}
         * 或 {@link Fragment#startActivityForResult(Intent, int)}，
         * 而不会使用 Activity 的相应的方法
         *
         * @param fragment 指定的Fragment
         * @return this
         */
        public Builder startWithFragment(Fragment fragment) {
            P.fragment = fragment;
            return this;
        }

        /**
         * 显式地指定分组名。
         *
         * NOTE: 会覆盖 path 中隐含的分组名。
         *
         * @param group 显式指定的分组名
         * @return this
         */
        public Builder group(String group) {
            P.group = group;
            return this;
        }

        public Builder path(String path) {
            P.path = path;
            return this;
        }

        public Builder setIntentFlags(int flags) {
            P.intentFlags = flags;
            return this;
        }

        public Builder addIntentFlags(int flags) {
            P.intentFlags |= flags;
            return this;
        }

        public Builder forResult(int requestCode) {
            P.forResult = true;
            P.requestCode = requestCode;
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

        public Builder parcelableArrayListExtra(String key, ArrayList<Parcelable> value) {
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
         * @param key key
         * @param value value
         * @return this
         */
        public Builder bundleExtra(String key, Bundle value) {
            P.myExtras().putBundle(key, value);
            return this;
        }

        //TODO: OTHER putXxx methods of Bundle

        public boolean go() {
            return P.go();
        }
    }

}
