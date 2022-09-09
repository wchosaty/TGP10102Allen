package idv.tgp10102.allen.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import idv.tgp10102.allen.Member;
import idv.tgp10102.allen.R;

public class EditCommentFragment extends Fragment {
    private EditText etComment;
    private ImageView ivComment,ivSend;
    private TextView test;

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

        if (getArguments() != null) {
            Member member = (Member) getArguments().getSerializable("member");
            test.setText(member.getStringName());
        }
    }

    private void findViews(View view) {
        etComment = view.findViewById(R.id.etComment);
        ivSend = view.findViewById(R.id.ivSend);
        test = view.findViewById(R.id.textView2);
    }
}