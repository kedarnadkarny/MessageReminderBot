package main.demov1;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class SpecialReminderFragment extends Fragment {

    static EditText etTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_special_reminder, container, false);
        etTitle = (EditText) view.findViewById(R.id.etDailyTitle);
        etTitle.setText("test");
        return view;
    }

    public static void updateSpecial(String mKey) {
        etTitle.setText(mKey);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
}
