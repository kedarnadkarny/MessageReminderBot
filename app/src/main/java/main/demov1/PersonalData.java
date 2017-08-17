package main.demov1;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PersonalData extends Application {
    private static String firstname;
    private static int num;

    public static String getFirstname() {
        return firstname;
    }

    public static void setFirstname(String firstname) {
        PersonalData.firstname = firstname;
    }

    public void savePreference(String user_id, Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("Uid", user_id);
        edit.commit();
    }

    public String checkPreferenceSet(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String Uid = sp.getString("Uid", "error");
        return Uid;
    }

    public void emptyPreference(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("Uid");
        editor.apply();
    }
}
