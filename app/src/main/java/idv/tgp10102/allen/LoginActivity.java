package idv.tgp10102.allen;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.util.Objects;


public class LoginActivity extends AppCompatActivity {
    private TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        tvMessage = findViewById(R.id.tvMessage_LoginMain);

    }

}