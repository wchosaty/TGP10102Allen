package idv.tgp10102.allen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private ImageButton ibSignOut;

    private static final String TAG = "Tag MainActivity";
    public static final String takePicCrop = "Crop" ,
            takePicOrigin = "Origin",
            LOCALNICKNAME = "localnickname";
    public static final int
            REQUEST_P1 = 1,REQUEST_P2 = 2,
            REQUEST_P3 = 3,REQUEST_P4 = 4;
    public static File myDirMember;
    private BottomNavigationView bottomNavigationView;
    public static Boolean remoteCould = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);
        myDirMember = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        remoteCould = false;
        findViews();
        handleBottomNavigationView();

        ibSignOut.setOnClickListener(v -> {

            auth.signOut();
            Intent intent = new Intent();
            intent.setClass(this, LoginActivity.class);
            startActivity(intent);
            this.finish();
        });

    }

    private void findViews() {
        bottomNavigationView = findViewById(R.id.bottomNavView);
        ibSignOut = findViewById(R.id.ibSignOut);

    }

    private void handleBottomNavigationView() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);

        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView,navController);
    }

}