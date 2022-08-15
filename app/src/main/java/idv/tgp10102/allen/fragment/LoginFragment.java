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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import idv.tgp10102.allen.LoginActivity;
import idv.tgp10102.allen.MainActivity;
import idv.tgp10102.allen.R;

public class LoginFragment extends Fragment {
    private final static String TAG = "TAG_LoginFragment";
    private GoogleSignInClient client;
    private FirebaseAuth auth;
    private Activity activity;
    private EditText etEmail, etPassword ,nickName;
    private TextView tvMessage;
    private Button btSingIn,btSignUp;
    private ImageView ivGoogleSignIn;

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
                        Intent intent = new Intent();
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
        findViews(view);
        handleButton();
    }

    private void handleButton() {
        ivGoogleSignIn.setOnClickListener(v -> {
            Intent intent = client.getSignInIntent();
            //跳出google
            signInGoogleLauncher.launch(intent);
        });
    }

    private void findViews(View view) {
        etEmail = view.findViewById(R.id.etEmail_login);
        etPassword = view.findViewById(R.id.etPassWord);
        nickName = view.findViewById(R.id.etNickName_login);
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