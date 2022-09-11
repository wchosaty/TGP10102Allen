package idv.tgp10102.allen.fragment;

import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Callable;

import idv.tgp10102.allen.Member;

public class CommentCallable implements Callable<Boolean> {
    private static final String TAG = "Tag CommentCallable";
    String nickName,photoName,comment;
    TextView tvNickname,tvComment;
    Member member;

    @Override
    public Boolean call() throws Exception {
        return getDBComment();
    }

    private Boolean getDBComment() {
        FirebaseFirestore.getInstance().collection("TGP101 02 Allen")
                .document("CommentRQ").collection("CommentRQ")
                .document(member.getNickname()).collection(member.getStringName())
                .get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){

                    }
                });
        return true;
    }
}
