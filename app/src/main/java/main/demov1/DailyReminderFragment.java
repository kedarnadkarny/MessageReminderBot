package main.demov1;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DailyReminderFragment extends Fragment {

    View rootView;
    OnMessageSetListener OnMessageSetListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_daily_reminder, container, false);

        return rootView;
    }

    public DailyReminderFragment() {

    }

    public static DailyReminderFragment newInstance() {
        DailyReminderFragment fragment = new DailyReminderFragment();
        return fragment;
    }

    public interface OnMessageSetListener {
        void setMessage(String message);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            OnMessageSetListener = (OnMessageSetListener) context;
        }
        catch (Exception e) {

        }
    }
}
