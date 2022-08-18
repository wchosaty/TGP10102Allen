package idv.tgp10102.allen.fragment;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import idv.tgp10102.allen.LoginActivity;
import idv.tgp10102.allen.MainActivity;
import idv.tgp10102.allen.R;
import idv.tgp10102.allen.User;

public class LoginFragment extends Fragment {
    private final static String TAG = "TAG_LoginFragment";
    private GoogleSignInClient client;
    private FirebaseAuth auth;
    private Activity activity;
    private EditText etEmail, etPassword ,etNickName,etPhoneNumber;
    private TextView tvMessage;
    private Button btSingIn,btSignUp;
    private ImageView ivGoogleSignIn;
    private User user;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private boolean flag= false;

    ActivityResultLauncher<Intent> signInGoogleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null) {
                        firebaseAuthWithGoogle(account);
                    } else {
                        Log.e(TAG, "GoogleSignInAccount is null");
                    }
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.e(TAG, e.toString());
                }
            }
    );

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    // 登入成功轉至下頁；失敗則顯示錯誤訊息
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        Intent intent = new Intent();
                        if (firebaseUser != null) {
                            String uid = task.getResult().getUser().getUid();
                            Log.d(TAG,"uid : " +uid);
                            this.user.setUid(uid);
                            FirebaseFirestore.getInstance()
                                    .collection(getString(R.string.app_name)+"users").document(uid)
                                    .set(this.user).addOnCompleteListener(taskGoogleInsertDB -> {
                                                if (taskGoogleInsertDB.isSuccessful()) {
                                                    Log.d(TAG, "taskGoogleInsertDB : Successful");
                                                }
                                    });
                        }

                        intent.setClass(activity, MainActivity.class);
                        startActivity(intent);
                        activity.finish();
                    } else {
                        Exception exception = task.getException();
                        String message = exception == null ? "Sign in fail." : exception.getMessage();
                        tvMessage.setText(message);
                    }
                });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
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
        return inflater.inflate(R.layout.fragment_login, container, false);
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
        //透過第三方Google登入
        ivGoogleSignIn.setOnClickListener(v -> {
            String nickName = etNickName.getText().toString();
            if (checkNickNameEmpty(nickName)) {
                return;
            }
            String email = etEmail.getText().toString();
            String passWord = etPassword.getText().toString();
            String nickNameG = etNickName.getText().toString();
            String phone = etPhoneNumber.getText().toString();
            this.user.setPhone(phone);
            this.user.setEmail(email);
            this.user.setNickName(nickNameG);

            Intent intent = client.getSignInIntent();
            //跳出google
            signInGoogleLauncher.launch(intent);
        });
        
        //Email signIn
        btSingIn.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String passWord = etPassword.getText().toString();
            signIn(email,passWord);
        });
        
        //Email signUp
        btSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String passWord = etPassword.getText().toString();
            String nickName = etNickName.getText().toString();
            User user = new User();
            String phone = etPhoneNumber.getText().toString();
            user.setPhone(phone);
            user.setEmail(email);
            user.setNickName(nickName);
            signUp(email,passWord,nickName,user);
        });

    }

    private void signIn(String email, String passWord) {
        if(checkEmailPasswordEmpty(email,passWord)){
            return;
        }
        auth.signInWithEmailAndPassword(email, passWord)
                .addOnCompleteListener(task -> {
                    // 登入成功轉至下頁；失敗則顯示錯誤訊息
                    if (task.isSuccessful()) {
                        Intent intent = new Intent();
                        intent.setClass(activity, MainActivity.class);
                        startActivity(intent);
                        activity.finish();
                    } else {
                        String message;
                        Exception exception = task.getException();
                        if (exception == null) {
                            message = "Sign in fail.";
                        } else {
                            String exceptionType;
                            // FirebaseAuthInvalidCredentialsException代表帳號驗證不成功，例如email格式不正確
                            if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                exceptionType = "Invalid Credential";
                            }
                            // FirebaseAuthInvalidUserException代表無此user，例如帳密錯誤
                            else if (exception instanceof FirebaseAuthInvalidUserException) {
                                exceptionType = "Invalid User";
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

    private void signUp(String email, String passWord,String nickName,User user) {
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
                            FirebaseFirestore.getInstance()
                                    .collection(getString(R.string.app_name)+"users").document(user.getUid())
                                    .set(user).addOnCompleteListener(taskInsertDB -> {
                                        if (taskInsertDB.isSuccessful()) {
                                            Log.d(TAG,"taskInsertDB : Successful");
                                        }
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

    private boolean checkNickNameEmpty(String nickName) {
        if (nickName.trim().isEmpty() ) {
            tvMessage.setText(R.string.textCheckNicknameEmpty);
            return true;
        } else {
            return false;
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

    private void findViews(View view) {
        etEmail = view.findViewById(R.id.etEmail_login);
        etPassword = view.findViewById(R.id.etPassWord);
        etPhoneNumber = view.findViewById(R.id.etPhoneNember);
        etNickName = view.findViewById(R.id.etNickName_login);
        tvMessage = view.findViewById(R.id.tvMessage_login);
        btSingIn = view.findViewById(R.id.btSignIn);
        btSignUp = view.findViewById(R.id.btSignUp);
        ivGoogleSignIn = view.findViewById(R.id.ivGoogleSignIn);
    }

    @Override
    public void onStart() {
        super.onStart();
// 重新檢查
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent().setClass(activity, MainActivity.class) );
            activity.finish();
        }
    }
}