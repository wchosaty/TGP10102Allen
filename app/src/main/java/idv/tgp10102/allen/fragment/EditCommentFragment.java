package idv.tgp10102.allen.fragment;

import static idv.tgp10102.allen.MainActivity.NAME;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import idv.tgp10102.allen.AccessCallable;
import idv.tgp10102.allen.MainActivity;
import idv.tgp10102.allen.Member;
import idv.tgp10102.allen.R;

public class EditCommentFragment extends Fragment {
    private EditText etComment;
    private ImageView ivComment,ivSend;
    private TextView test;
    private Member member;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_comment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getArguments() != null) {
            member = (Member) getArguments().getSerializable("member");
            test.setText(member.getStringName());
        }
        // 送出留言...
        ivSend.setOnClickListener(v -> {
            Map<String, Object> data = new HashMap<>();
            data.put("user", MainActivity.CURRENTNICKNAME);
            if (etComment.getText() != null && etComment.getText().toString() != null) {
                data.put("comment", etComment.getText().toString());
                FirebaseFirestore.getInstance().collection(getString(R.string.app_name)).document("CommentRQ").collection("CommentRQ")
                        .document(member.getNickname()).collection(member.getStringName()).document(String.valueOf(System.currentTimeMillis())).set(data).addOnCompleteListener(
                                task -> {
                                    if (task.isSuccessful()) {
                                        // Bundle回去顯示
                                        Bundle bundle = new Bundle();
                                        bundle.putString(NAME,member.getNickname());
                                        Navigation.findNavController(v).navigate(R.id.action_editCommentFragment_to_mitDetail,bundle);
                                    }
                                });
            }
        });
    }

    private void findViews(View view) {
        etComment = view.findViewById(R.id.etComment);
        ivSend = view.findViewById(R.id.ivSend);
        test = view.findViewById(R.id.textView2);
    }
}