package idv.tgp10102.allen.fragment;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Callable;


public class StringCallable implements Callable<Boolean> {
    private static final String TAG = "Tag ImageCallable";
    String nickName,photoName;
    ImageView imageView;
    TextView textView;
    int code;

    public StringCallable(String nickName, String photoName, ImageView imageView, TextView textView, int code) {
        this.nickName = nickName;
        this.photoName = photoName;
        this.imageView = imageView;
        this.textView = textView;
        this.code = code;
    }

    @Override
    public Boolean call() throws Exception {
        switch (code){
            case 1:
                return getDBThumb();
            case 2:
                return getDBComment();

            default:
                return true;
        }
    }

    private Boolean getDBThumb() {
        FirebaseFirestore.getInstance().collection("TGP101 02 Allen")
                .document("ThumbRQ").collection("ThumbRQ")
                .document(nickName).collection(photoName).
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

    private Boolean getDBComment() {
        FirebaseFirestore.getInstance().collection("TGP101 02 Allen")
                .document("CommentRQ").collection("CommentRQ")
                .document(nickName).collection(photoName).
                get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        if(task.getResult().size() > 0){
                            imageView.setVisibility(View.VISIBLE);
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(""+task.getResult().size());
                        }
                    }
                });
        return  true;
    }
}
