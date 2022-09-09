package idv.tgp10102.allen.fragment;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Callable;


public class StringCallable implements Callable<Boolean> {
    private static final String TAG = "Tag ImageCallable";
    String photoName,nickname;
    ImageView imageView;
    TextView textView;
    int code;

    public StringCallable(String nickname, String photoName, ImageView imageView, TextView textView, int code) {
        this.nickname = nickname;
        this.photoName = photoName;
        this.imageView = imageView;
        this.textView = textView;
        this.code = code;
    }

    @Override
    public Boolean call() throws Exception {
        if(code ==1){
            return getDBContent();
        }else {
            return true;
        }
    }

    private Boolean getDBContent() {
        FirebaseFirestore.getInstance().collection("TGP101 02 Allen")
                .document("ThumbRQ").collection("ThumbRQ")
                .document(nickname).collection(photoName).
                get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        if(task.getResult().size() > 0){
                            imageView.setVisibility(View.VISIBLE);
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(""+task.getResult().size());
                        }
                    }
                });
        return true;
    }
}
