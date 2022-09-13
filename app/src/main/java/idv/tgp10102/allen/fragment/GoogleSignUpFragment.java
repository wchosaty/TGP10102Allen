package idv.tgp10102.allen.fragment;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;

import idv.tgp10102.allen.MainActivity;
import idv.tgp10102.allen.R;
import idv.tgp10102.allen.User;

public class GoogleSignUpFragment extends Fragment {
    private final static String TAG = "TAG_G-SignUpFragment";
    private Activity activity;
    private FirebaseAuth auth;
    private GoogleSignInClient client;
    private User user;
    private TextView tvMessage;
    private Button btSignIn,btSignUp;
    private EditText etNickname,etPhone;
    private ImageButton ibBack;
    private ImageView ivNicknamePic;
    private Uri uriUserPicture;
    private FirebaseStorage storage;
    private FirebaseFirestore db;

    ActivityResultLauncher<Intent> pickPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::pickPictureResult);

    private void pickPictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            File copyDir = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS +"/user/");
            Uri uri = result.getData().getData();
            if (uri != null) {
                uriUserPicture = uri;
                // 提供圖檔的URI，ImageView可以直接顯示
                ivNicknamePic.setImageURI(uri);
            }
        }
    }

    ActivityResultLauncher<Intent> signUpGoogleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null) {
                        firebaseAuthWithGoogleUp(account);
                    } else {
                        Log.e(TAG, "GoogleSignInAccount is null");
                    }
                } catch (ApiException e) {
                    Log.e(TAG, e.toString());
                }
            }
    );

    private void firebaseAuthWithGoogleUp(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(taskUp -> {
                    // 登入成功轉至下頁；失敗則顯示錯誤訊息
                    if (taskUp.isSuccessful()) {
                        FirebaseUser firebaseUser = taskUp.getResult().getUser();
                        Intent intent = new Intent();
                        if (firebaseUser != null) {
                            String uid = taskUp.getResult().getUser().getUid();
                            Log.d(TAG,"uid : " +uid);
                            this.user.setUid(uid);
                            final String imagePath = getString(R.string.app_name) + "/userPicture/"+this.user.getUid();
                            this.user.setNicknameCloudPic(imagePath);
                            FirebaseFirestore.getInstance()
                                    .collection(getString(R.string.app_name)+"users").document(uid)
                                    .set(this.user).addOnCompleteListener(taskGoogleInsertDB -> {
                                        if (taskGoogleInsertDB.isSuccessful()) {
                                            Log.d(TAG, "taskGoogleInsertDB : Successful");
                                        }
                                    });
                            storage.getReference().child(imagePath).putFile(uriUserPicture)
                                    .addOnCompleteListener(taskNickPic -> {
                                        if (taskNickPic.isSuccessful()) {
                                            Log.d(TAG, "task.isSuccessful() ");
                                        } else {
                                            Log.d(TAG, "storage.getReference() : Fail ");
                                        }
                                        // 無論圖檔上傳成功或失敗都要將文字資料新增至DB
                                    });
                        }

                        intent.setClass(activity, MainActivity.class);
                        startActivity(intent);
                        activity.finish();
                    } else {
                        Exception exception = taskUp.getException();
                        String message = exception == null ? "Sign in fail." : exception.getMessage();
                        tvMessage.setText(message);
                    }
                });
    }

    ActivityResultLauncher<Intent> signInGoogleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null) {
                        firebaseAuthWithGoogleIn(account);
                    } else {
                        Log.e(TAG, "GoogleSignInAccount is null");
                    }
                } catch (ApiException e) {
                    Log.e(TAG, e.toString());
                }
            }
    );

    private void firebaseAuthWithGoogleIn(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(taskIn -> {
                    // 登入成功轉至下頁；失敗則顯示錯誤訊息
                    if (taskIn.isSuccessful()) {
                        Bundle bundle = new Bundle();
                        FirebaseUser firebaseUser = taskIn.getResult().getUser();
                        Intent intent = new Intent();
                        if (firebaseUser != null) {
                            String uid = taskIn.getResult().getUser().getUid();
                            Log.d(TAG,"uid : " +uid);
                            FirebaseFirestore.getInstance()
                                    .collection(getString(R.string.app_name)+"users").document(uid)
                                    .get().addOnCompleteListener(taskGoogleInsertDB -> {
                                        if (taskGoogleInsertDB.isSuccessful()) {
                                            Log.d(TAG, "taskGoogleInsertDB signIn : Successful");
                                            }
                                    });
                            intent.setClass(activity, MainActivity.class);
                            startActivity(intent);
                            activity.finish();
                        }

                    } else {
                        Exception exception = taskIn.getException();
                        String message = exception == null ? "Sign in fail." : exception.getMessage();
                        tvMessage.setText(message);
                    }
                });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // 由google-services.json轉出，有時會編譯失敗，但不影響執行
                .requestIdToken(getString(R.string.default_web_client_id))
                // 要求輸入email
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(requireActivity(), options);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_google_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        user = new User();
        findViews(view);
        handleButton();
    }

    private void handleButton() {

        ivNicknamePic.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            try {
                pickPictureLauncher.launch(intent);
            } catch (ActivityNotFoundException e) {
                Log.d(TAG, "pickPictureLauncher : "+ R.string.textNoImagePickerAppFound);
                tvMessage.setText(R.string.textNoImagePickerAppFound);
            }
        });

        ibBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_googleSignUpFragment_to_loginFragment);
        });

        btSignIn.setOnClickListener(v -> {
            Intent intent = client.getSignInIntent();
            //跳出google
            signInGoogleLauncher.launch(intent);
        });

        btSignUp.setOnClickListener(v -> {
            String nickName = etNickname.getText().toString();
            if (checkNickNameEmpty(nickName)) {
                return;
            }
            String nickname = etNickname.getText().toString();
            String phone = etPhone.getText().toString();
            this.user.setPhone(phone);
            this.user.setNickName(nickname);
            Intent intent = client.getSignInIntent();
            //跳出google
            signUpGoogleLauncher.launch(intent);
        });
    }

    private boolean checkNickNameEmpty(String nickName) {
        if (nickName.trim().isEmpty() ) {
            tvMessage.setText(R.string.textCheckNicknameEmpty);
            return true;
        } else {
            return false;
        }
    }

    private void findViews(View view) {
        etNickname = view.findViewById(R.id.etNickName_google);
        etPhone = view.findViewById(R.id.etPhoneNember_google);
        btSignIn = view.findViewById(R.id.btSignIn_google);
        btSignUp = view.findViewById(R.id.btSignUp_google);
        ibBack = view.findViewById(R.id.ibBack_google);
        tvMessage = view.findViewById(R.id.tvMessage_google);

        ivNicknamePic = view.findViewById(R.id.ivNickname_google);
    }
}