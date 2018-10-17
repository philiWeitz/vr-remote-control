package remote.vr.com.remote_android.main;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPreferencesUtil {

    public static final String KEY_ROOM_ID = "room_id";
    public static final String KEY_CLIENT_ID = "client_id";
    private static final String STORAGE_NAME = "vr_remote_session_storage";


    private SharedPreferencesUtil() {

    }


    public static void setPreference(Context ctx, String key, String value) {
        SharedPreferences data = ctx.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = data.edit();
        editor.putString(key, value);
        editor.apply();
    }


    public static String getPreference(Context ctx, String key) {
        SharedPreferences data = ctx.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
        return data.getString(key, "");
    }


    public static void deletePreference(Context ctx, String key) {
        SharedPreferences data = ctx.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = data.edit();
        editor.remove(key);
        editor.apply();
    }


    public static boolean hasPreference(Context ctx, String key) {
        SharedPreferences data = ctx.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
        return data.contains(key);
    }
}