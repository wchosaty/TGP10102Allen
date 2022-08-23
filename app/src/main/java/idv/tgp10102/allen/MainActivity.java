package idv.tgp10102.allen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private ImageButton ibSignOut;
    private ImageView ivUser;
    private TextView tvUserNickName;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    public static User userNickname;

    private static final String TAG = "Tag MainActivity";
    public static final String takePicCrop = "Crop" ,
            takePicOrigin = "Origin",
            LOCALNICKNAME = "localnickname",GEUST = "guest";
    public static final String
            READ = "read",CREATENEW = "createNewFile",
            UPDATE = "update",DELETE = "delete",
            NAME = "name",WORKTYPE = "worktype",
            SAVE ="save",ADD = "add";
    public static String CURRENTNICKNAME;
    private BottomNavigationView bottomNavigationView;
    public static Boolean remoteCould;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        setContentView(R.layout.activity_main);
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
        tvUserNickName = findViewById(R.id.tvCurrentPhotoNick_Main);
        ivUser = findViewById(R.id.ivCurrentPhotoNick_Detail);

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
                            this.userNickname = taskUserData.getResult().toObject(User.class);
                            if(!Objects.equals(userNickname,null)){
                                CURRENTNICKNAME = userNickname.getNickName().trim();
                                tvUserNickName.setText(userNickname.getNickName());
                            }else {
                                tvUserNickName.setText(getString(R.string.textCheckNicknameEmpty));
                            }
                        }else {
                            tvUserNickName.setText(getString(R.string.textCheckNicknameEmpty));
                        }
                    });
            final int MEGABYTE = 4 * 1024 * 1024;
            storage.getReference().child(getString(R.string.app_name)+"/userPicture/"+userUid)
                    .getBytes(MEGABYTE).addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null){
                            byte[] bytes = task.getResult();
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            ivUser.setImageBitmap(bitmap);
                        }else {
                            Log.e(TAG, "nicknamePicture : downloadStrage Fail");
                        }
                    });
        }
    }



}