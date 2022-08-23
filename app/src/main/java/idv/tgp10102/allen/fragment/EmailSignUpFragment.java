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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import idv.tgp10102.allen.MainActivity;
import idv.tgp10102.allen.R;
import idv.tgp10102.allen.User;

public class EmailSignUpFragment extends Fragment {
    private final static String TAG = "TAG_EmailSignUpFragment";
    private Activity activity;
    private EditText etEmail,etPassword,etPhone,etNickname;
    private ImageButton ibBack;
    private ImageView ivNicknamePic;
    private Button btSignUp;
    private FirebaseAuth auth;
    private TextView tvMessage;
    private Uri uriUserPicture;
    private FirebaseStorage storage;
    private FirebaseFirestore db;

    ActivityResultLauncher<Intent> pickPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::pickPictureResult);

    private void pickPictureResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            File copyDir = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS +"/user/");
            File copyDest = new File(copyDir,"userPicture.jpg");
            Uri uri = result.getData().getData();
            File uritoFile  = null;
            if (uri != null) {
                uriUserPicture = uri;
                // 提供圖檔的URI，ImageView可以直接顯示
                ivNicknamePic.setImageURI(uri);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_email_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        findViews(view);
        handleButton();
    }

    private void findViews(View view) {
        etEmail = view.findViewById(R.id.etEmail_loginEUP);
        etPassword = view.findViewById(R.id.etPassWordEUP);
        etPhone = view.findViewById(R.id.etPhoneNemberEUP);
        etNickname = view.findViewById(R.id.etNickName_EUP);
        btSignUp = view.findViewById(R.id.btSignUpEmail);
        ibBack = view.findViewById(R.id.ibBack_EmailSignUp);
        tvMessage = view.findViewById(R.id.tvMessage_EmailsifnUp);
        ivNicknamePic = view.findViewById(R.id.ivNickname_EUP);
    }

    private void handleButton() {

        ibBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_emailSignUpFragment_to_loginFragment);
        });

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

        btSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String passWord = etPassword.getText().toString();
            String nickName = etNickname.getText().toString();
            User user = new User();
            String phone = etPhone.getText().toString();
            user.setPhone(phone);
            user.setEmail(email);
            user.setNickName(nickName);
            signUp(email,passWord,nickName,user);
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
    private void copy(File file,StringBuilder sbDest) {
        if( !file.exists() ){
            Log.d(TAG,"copy2 : ");
            return;
        }
        Log.d(TAG,"copy2 : ");
        try(
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(String.valueOf(file.toString())));
                FileOutputStream fos = new FileOutputStream(String.valueOf(sbDest));
                BufferedOutputStream bos = new BufferedOutputStream(fos);
        ) {
            byte[] b = new byte[bis.available()];
            bis.read(b);
            bos.write(b);
            bos.flush();

        }catch (Exception e){
            Log.e(TAG,e.toString());
            Log.e(TAG, Arrays.toString(e.getStackTrace()));
        }
    }

    private boolean checkEmailPasswordEmpty(String email, String password) {
        if (email.trim().isEmpty() || password.trim().isEmpty()) {
            tvMessage.setText(R.string.textCheckEmailPasswordEmpty);
            return true;
        } else {
            return false;
        }
    }

    private void signUp(String email, String passWord, String nickName, User user) {
        if (checkEmailPasswordEmpty(email, passWord)) {
            return;
        }
        if (checkNickNameEmpty(nickName)) {
            return;
        }
        /* 利用user輸入的email與password建立新的帳號 */
        auth.createUserWithEmailAndPassword(email, passWord)
                .addOnCompleteListener(task -> {
                    // 建立成功則轉至下頁；失敗則顯示錯誤訊息
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        // Save uid
                        if (firebaseUser != null) {
                            String uid = task.getResult().getUser().getUid();
                            user.setUid(uid);
                            // 大頭照
                            final String imagePath = getString(R.string.app_name) + "/userPicture/"+user.getUid();
                            user.setNicknameCloudPic(imagePath);
                            FirebaseFirestore.getInstance()
                                    .collection(getString(R.string.app_name)+"users").document(user.getUid())
                                    .set(user).addOnCompleteListener(taskInsertDB -> {
                                        if (taskInsertDB.isSuccessful()) {
                                            Log.d(TAG,"taskInsertDB : Successful");
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
                        // 註冊成功跳頁




                        Intent intent = new Intent();
                        intent.setClass(activity, MainActivity.class);
                        startActivity(intent);
                        activity.finish();

                    } else {
                        String message;
                        Exception exception = task.getException();
                        if (exception == null) {
                            message = "Register fail.";
                        } else {
                            String exceptionType;
                            // FirebaseAuthInvalidCredentialsException 代表帳號驗證不成功，例如email格式不正確
                            if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                exceptionType = "Invalid Credential";
                            }
                            // FirebaseAuthInvalidUserException 代表無此user，例如帳密錯誤
                            else if (exception instanceof FirebaseAuthInvalidUserException) {
                                exceptionType = "Invalid User";
                            }
                            // FirebaseAuthUserCollisionException 代表此帳號已被使用
                            else if (exception instanceof FirebaseAuthUserCollisionException) {
                                exceptionType = "User Collision";
                            } else {
                                exceptionType = exception.getClass().toString();
                            }
                            message = exceptionType + ": " + exception.getLocalizedMessage();
                        }
                        Log.e(TAG, message);
                        tvMessage.setText(message);
                    }
                });
    }
}