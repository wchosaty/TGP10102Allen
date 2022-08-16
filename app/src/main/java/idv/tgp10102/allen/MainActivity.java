package idv.tgp10102.allen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private ImageButton ibSignOut;
    private ImageView ivUser;
    private TextView tvUserNickName;
    private FirebaseFirestore db;


    private static final String TAG = "Tag MainActivity";
    public static final String takePicCrop = "Crop" ,
            takePicOrigin = "Origin",
            LOCALNICKNAME = "localnickname";
    public static final int
            REQUEST_P1 = 1,REQUEST_P2 = 2,
            REQUEST_P3 = 3,REQUEST_P4 = 4;
    public static File myDirMember;
    private BottomNavigationView bottomNavigationView;
    public static Boolean remoteCould;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
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
        tvUserNickName = findViewById(R.id.tvUesrNickName_Main);
        ivUser = findViewById(R.id.ivUesr_Main);

    }

    private void handleBottomNavigationView() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);

        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView,navController);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent().setClass(this, LoginActivity.class) );
            this.finish();
        }else {
            // 顯示User
            String userUid = auth.getUid();
            db.collection(getString(R.string.app_name)+"users").document(userUid)
                    .get().addOnCompleteListener(taskUserData -> {
                        if(taskUserData.isSuccessful() && taskUserData.getResult()!= null){
                            Log.d(TAG,"taskUserData : Successful");
                            User userNickname = taskUserData.getResult().toObject(User.class);
                            tvUserNickName.setText(userNickname.getNickName());

                        }
                    });

        }
    }
}