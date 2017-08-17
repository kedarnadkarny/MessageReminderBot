package main.demov1;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ReminderTabActivity extends AppCompatActivity implements DailyReminderFragment.OnMessageSetListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    String type = "";
    static String key = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_tab);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        Intent intent = getIntent();
        if(intent.hasExtra("key")) {
            key = intent.getStringExtra("key");
            type = intent.getStringExtra("etype");
            Log.d("mKey", key);
            if(type.equals("daily")) {
                mViewPager.setCurrentItem(0);
                DailyReminderFragment srf = new DailyReminderFragment();
                Bundle bundle = new Bundle();
                bundle.putString("key", key);
                srf.setArguments(bundle);
            }
            else if(type.equals("special")) {
                mViewPager.setCurrentItem(2);
                SpecialReminderFragment srf = new SpecialReminderFragment();
                Bundle bundle = new Bundle();
                bundle.putString("key", key);
                srf.setArguments(bundle);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reminder_tab, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setMessage(String message) {
        Log.d("messageSet", message);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements View.OnClickListener {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        View rootView;
        private FirebaseAuth mAuth;
        DailyReminderContainer daily;
        SpecialReminderContainer special;
        Button btn_daily_update;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            mAuth = FirebaseAuth.getInstance();

            if(getArguments().getInt(ARG_SECTION_NUMBER)==1) {
                daily = new DailyReminderContainer();
                rootView = inflater.inflate(R.layout.fragment_daily_reminder, container, false);
                Button btn_save = (Button) rootView.findViewById(R.id.btn_save_daily);
                btn_daily_update = (Button) rootView.findViewById(R.id.btn_daily_update);
                // TODO: invisibility not working
                btn_daily_update.setVisibility(View.INVISIBLE);
                btn_save.setOnClickListener(this);
                loadDailyReminderData(key);
                return rootView;
            }
            else if(getArguments().getInt(ARG_SECTION_NUMBER)==2) {
                special = new SpecialReminderContainer();
                rootView = inflater.inflate(R.layout.fragment_special_reminder, container, false);
                Button btn_save_special = (Button) rootView.findViewById(R.id.btn_save_special);
                btn_save_special.setOnClickListener(this);
                loadSpecialReminder(key);
                return rootView;
            }
            else {
                rootView = inflater.inflate(R.layout.fragment_reminder_tab, container, false);
                TextView textView = (TextView) rootView.findViewById(R.id.section_label);
                textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
                return rootView;
            }
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_save_daily:
                    addDailyReminder();
                    //updateDailyReminder();
                    break;
                case R.id.btn_save_special:
                    //Toast.makeText(getContext(), "TAP!", Toast.LENGTH_LONG).show();
                    addSpecialReminder();
                    //updateSpecialReminder();
            }
        }

        public void loadDailyReminderData(String key) {
            DatabaseReference mEvents = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("reminders").child(key);
            mEvents.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // TODO: this visibility is also not working
                    btn_daily_update.setVisibility(View.VISIBLE);
                    EditText etTitle = (EditText) rootView.findViewById(R.id.etDailyTitle);
                    etTitle.setText(dataSnapshot.child("title").getValue(String.class));
                    TimePicker timePicker = (TimePicker) rootView.findViewById(R.id.timePicker);

                    try {
                        timePicker.setHour(Integer.parseInt(dataSnapshot.child("hour").getValue(String.class)));
                        timePicker.setMinute(Integer.parseInt(dataSnapshot.child("minute").getValue(String.class)));
                    }
                    catch (Exception e) {}
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void addDailyReminder() {
            DatabaseReference mEvents = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("reminders");
            EditText etTitle = ((EditText) rootView.findViewById(R.id.etDailyTitle));
            TimePicker timePicker = (TimePicker) rootView.findViewById(R.id.timePicker);
            daily.setTitle(etTitle.getText().toString().trim());
            daily.setHour(String.valueOf(timePicker.getHour()));
            daily.setMinute(String.valueOf(timePicker.getMinute()));
            daily.setEventType("daily");
            mEvents.push().setValue(daily);
            etTitle.setText("");
            Toast.makeText(getContext(), "Daily Reminder Added!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getContext(), RemindersActivity.class));
        }

        public void addSpecialReminder() {
            DatabaseReference mEvents = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("reminders");
            String title = ((EditText) rootView.findViewById(R.id.etSpecialTitle)).getText().toString().trim();
            DatePicker datePicker = (DatePicker) rootView.findViewById(R.id.datePicker);
            String day = String.valueOf(datePicker.getDayOfMonth());
            String month = String.valueOf(datePicker.getMonth());
            String year = String.valueOf(datePicker.getYear());
            special.setTitle(title);
            special.setDay(day);
            special.setMonth(month);
            special.setYear(year);
            special.setEventType("special");
            mEvents.push().setValue(special);
            Toast.makeText(getContext(), "Special Reminder Added!", Toast.LENGTH_LONG).show();

            startActivity(new Intent(getContext(), RemindersActivity.class));
        }

        public void updateDailyReminder() {
            DatabaseReference mEvents = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("reminders").child(key);

            mEvents.child("title").setValue(((EditText) rootView.findViewById(R.id.etDailyTitle)).getText().toString().trim());
            mEvents.child("hour").setValue(((TimePicker) rootView.findViewById(R.id.timePicker)).getHour());
            mEvents.child("minute").setValue(((TimePicker) rootView.findViewById(R.id.timePicker)).getMinute());
            startActivity(new Intent(getContext(), RemindersActivity.class));
        }

        public void loadSpecialReminder(String key) {
            final EditText etTitle = (EditText) rootView.findViewById(R.id.etSpecialTitle);
            final DatePicker datePicker = (DatePicker) rootView.findViewById(R.id.datePicker);

            DatabaseReference mEvents = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("reminders").child(key);
            mEvents.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int day = 0, month = 0, year = 0;
                    try {
                        etTitle.setText(dataSnapshot.child("title").getValue(String.class));
                        day = Integer.parseInt(dataSnapshot.child("day").getValue(String.class));
                        month = Integer.parseInt(dataSnapshot.child("month").getValue(String.class));
                        year = Integer.parseInt(dataSnapshot.child("year").getValue(String.class));
                        datePicker.updateDate(day, month, year);
                    }
                    catch (Exception e) {}
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        // TODO: update logic for both special and daily reminder
        public void updateSpecialReminder() {
            DatabaseReference mEvents = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("reminders").child(key);
            mEvents.child("title").setValue(((EditText) rootView.findViewById(R.id.etSpecialTitle)).getText().toString().trim());
            mEvents.child("day").setValue(((DatePicker) rootView.findViewById(R.id.datePicker)).getDayOfMonth());
            mEvents.child("month").setValue(((DatePicker) rootView.findViewById(R.id.datePicker)).getMonth());
            mEvents.child("year").setValue(((DatePicker) rootView.findViewById(R.id.datePicker)).getYear());
            startActivity(new Intent(getContext(), RemindersActivity.class));
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "DAILY REMINDER";
                case 1:
                    return "SPECIAL REMINDER";
            }
            return null;
        }
    }
}
