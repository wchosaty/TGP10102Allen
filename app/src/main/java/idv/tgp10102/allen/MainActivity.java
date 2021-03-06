package idv.tgp10102.allen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.os.Environment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Tag MainActivity";
    public static final String takePicCrop = "Crop" ,
            takePicOrigin = "Origin",
            LOCALNICKNAME = "localnickname";
    public static final int
            REQUEST_P1 = 1,REQUEST_P2 = 2,
            REQUEST_P3 = 3,REQUEST_P4 = 4;
    public static File myDirMember;
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myDirMember = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        findViews();

        handleBottomNavigationView();
    }

    private void findViews() {
        bottomNavigationView = findViewById(R.id.bottomNavView);
    }

    private void handleBottomNavigationView() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);

        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView,navController);
    }

}