package idv.tgp10102.allen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


public class LoginActivity extends AppCompatActivity {
    private final String TAG = "Tag LoginActivity";
    private TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        tvMessage = findViewById(R.id.tvMessage_LoginMain);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String dataNickname = bundle.getString("Nickname");
            String dataPhotoName = bundle.getString("StringName");

            Log.d(TAG, "Nickname: " + dataNickname);
            Log.d(TAG,"PhotoName : "+ dataPhotoName);
            SharedPreferences sharedPreferences = getSharedPreferences("preference",MODE_PRIVATE);
            sharedPreferences.edit().putString("Nickname",dataNickname).putString("PhotoName",dataPhotoName)
                    .putBoolean("status",true).apply();
        }

    }

}