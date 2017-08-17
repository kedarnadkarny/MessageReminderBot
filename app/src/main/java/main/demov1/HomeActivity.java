package main.demov1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Button btnShare, btnNext, btnBack;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    String image_url = "";
    Bitmap myBitmap;
    private ImageView imageView;
    TextView etHeaderName;
    private List<String> imageURLs = new ArrayList<String>();
    int i=0;
    int length = 0;
    String currentImage = "";
    private static HomeActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        instance = this;

        mAuth = FirebaseAuth.getInstance();
        imageView = (ImageView) findViewById(R.id.imageView);

        etHeaderName = (TextView) findViewById(R.id.etHeaderName);
        btnShare = (Button) findViewById(R.id.btnShare);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnBack = (Button) findViewById(R.id.btnBack);

        //btnBack.setVisibility(View.INVISIBLE);

        // TODO: Limit first n images only. Have to use Query
        mDatabase = FirebaseDatabase.getInstance().getReference().child("images");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap: dataSnapshot.getChildren()) {
                    image_url = (String) snap.getValue();
                    imageURLs.add(image_url);
                }
                length = imageURLs.size();
                Picasso.with(HomeActivity.this).load(imageURLs.get(i)).into(imageView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    downloadImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        PersonalData data = new PersonalData();
        String name = data.getFirstname();

        // TODO Get ID of component from different view and setText. Below code not working
        /*View inflatedView = getLayoutInflater().inflate(R.layout.nav_header_home, null);
        TextView text = (TextView) inflatedView.findViewById(R.id.etHeaderName);
        text.setText("Hello!");*/

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadNextImage();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: need to implement this functionality

                // sample alarm testing
                // TODO: This notification works. Needs implementation changes like removing and it's layout
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 18);
                calendar.set(Calendar.MINUTE, 39);
                calendar.set(Calendar.SECOND, 10);

                Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
            }
        });

    }


    // TODO: Remind alarm logic

    private void loadNextImage() {
        if(i>=length-1) {
            i=-1;
        }
        if(btnBack.getVisibility() == View.INVISIBLE) {
            btnShare.setVisibility(View.VISIBLE);
        }
        i = i+1;
        Picasso.with(HomeActivity.this).load(imageURLs.get(i)).into(imageView);
        image_url = imageURLs.get(i);
    }

    private void downloadImage() throws IOException {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference httpsReference = storage.getReferenceFromUrl(imageURLs.get(i));
        final File localFile = new File(getApplicationContext().getFilesDir(), "images");
        httpsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.v("response", "Success");
                Log.v("response", localFile.getAbsolutePath());
                myBitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                String pathofBmp = MediaStore.Images.Media.insertImage(getContentResolver(), myBitmap,"title", null);
                Uri bmpUri = Uri.parse(pathofBmp);
                intent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                Intent chooser = Intent.createChooser(intent, "Share...");
                if(intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.v("response", "Failure");
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(HomeActivity.this, HomeActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        } else if (id == R.id.nav_reminders) {
            startActivity(new Intent(HomeActivity.this, RemindersActivity.class));
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            PersonalData data = new PersonalData();
            data.emptyPreference(getApplicationContext());
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        } else if(id == R.id.nav_share) {
            Toast.makeText(getApplicationContext(), "SHARE", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String shareBody = "Your body here";
            String shareSub = "Your subject here";
            intent.putExtra(Intent.EXTRA_SUBJECT, shareBody);
            intent.putExtra(Intent.EXTRA_TEXT, shareSub);
            startActivity(Intent.createChooser(intent, "Share using"));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static HomeActivity getInstance() {
        return instance;
    }
    
    // TODO: Delete App Data. This logic is not working. Find out the location of the image stored in phone.
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("flamecycle", "paused");
        File cacheDirectory = getCacheDir();
        File applicationDirectory = new File(cacheDirectory.getParent());
        if(applicationDirectory.exists()) {
            String[] fileNames = applicationDirectory.list();
            for (String fileName : fileNames) {
                if (!fileName.equals("lib")) {
                    deleteFile(new File(applicationDirectory, fileName));
                }
            }
        }

    }

    private static boolean deleteFile(File file) {
        boolean deletedAll = true;
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                for (int i = 0; i < children.length; i++) {
                    deletedAll = deleteFile(new File(file, children[i])) && deletedAll;
                }
            } else {
                deletedAll = file.delete();
            }
        }

        return deletedAll;
    }
}
