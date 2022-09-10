package idv.tgp10102.allen.fragment;

import static idv.tgp10102.allen.MainActivity.NAME;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import idv.tgp10102.allen.AccessCallable;
import idv.tgp10102.allen.CommentObject;
import idv.tgp10102.allen.MainActivity;
import idv.tgp10102.allen.Member;
import idv.tgp10102.allen.R;

public class EditCommentFragment extends Fragment {
    private static final String TAG = "Tag EditCommentFragment";
    private Activity activity;
    private EditText etComment;
    private ImageView ivSend;
    private Member member;
    private RecyclerView recyclerViewComment,recyclerViewEmoji;
    private Map<String,Object> mapEmoji;
    private StringBuilder sbAddEmojiToComment;

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
        activity = getActivity();
        mapEmoji = new HashMap<>();
        findViews(view);
        handleViews();
    }

    private void handleViews() {
        recyclerViewComment.setLayoutManager(new LinearLayoutManager(activity));
//        recyclerViewComment.setAdapter(new CloudListFragment.MyCloudListAdapter(activity, cloudNicknamePersonList));
        recyclerViewEmoji.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL));
        MyEmojiAdapter myEmojiAdapter = new MyEmojiAdapter(activity,mapEmoji);
        recyclerViewEmoji.setAdapter(myEmojiAdapter);

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

    @Override
    public void onStart() {
        super.onStart();
        if (getArguments() != null) {
            member = (Member) getArguments().getSerializable("member");
        }

        mapEmoji = new HashMap<>();
        loadEmoji();
    }

    private void loadEmoji() {
        FirebaseFirestore.getInstance().collection(getString(R.string.app_name)).document("Emoji")
                .get().addOnCompleteListener(
                        task -> {
                            if(task.isSuccessful()){
                                if(task.getResult() != null){
                                   mapEmoji =  task.getResult().getData();
                                    Log.d(TAG,"mapEmoji : "+ mapEmoji.get("a") + " size : " + mapEmoji.size());

                                    MyEmojiAdapter emojiAdapter = (MyEmojiAdapter) recyclerViewEmoji.getAdapter();
                                    emojiAdapter.setMyEmojiAdapter(mapEmoji);
                                    emojiAdapter.notifyDataSetChanged();
                                }
                            }
                        });
    }

    private void findViews(View view) {
        etComment = view.findViewById(R.id.etComment);
        ivSend = view.findViewById(R.id.ivSend);
        recyclerViewComment = view.findViewById(R.id.recyclerView_Comment);
        recyclerViewEmoji = view.findViewById(R.id.recyclerView_Emoji);
    }

    // Commnet
    class MyCommentAdapter extends RecyclerView.Adapter<MyCommentAdapter.MyCommentViewHolder> {
        Context context;
        List<CommentObject> list;

        @Override
        public int getItemCount() {
            return 0;
        }

        @NonNull
        @Override
        public MyCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View itemView = layoutInflater.inflate(R.layout.item_comment,parent,false);
            return new MyCommentViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyCommentViewHolder holder, int position) {
        }

        class MyCommentViewHolder extends RecyclerView.ViewHolder {

            public MyCommentViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }

    class MyEmojiAdapter extends RecyclerView.Adapter<MyEmojiAdapter.MyEmojiViewHolder> {
        Context context;
        Map<String,Object> map;

        @Override
        public int getItemCount() {
            Log.d(TAG,"getItemCount  map.size() :" +  map.size() );
            return map == null ? 0 : map.size();
        }

        public MyEmojiAdapter(Context context, Map<String, Object> map) {
            this.context = context;
            this.map = map;
        }

        public void setMyEmojiAdapter(Map<String,Object> map) {
            this.map = map;
        }

        @NonNull
        @Override
        public MyEmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View itemView = layoutInflater.inflate(R.layout.item_emoji,parent,false);
            return new MyEmojiViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyEmojiViewHolder holder, int position) {
            char ch = (char) (97 +position);
            String s = Character.toString(ch);
            Log.d(TAG,"onBindViewHolder : "+ s + " / " +position);
            holder.tvEmoji.setText( (String) map.get(s) );

            // 監聽選擇表情圖案
            holder.tvEmoji.setOnClickListener(v -> {
                sbAddEmojiToComment = new StringBuilder();
                sbAddEmojiToComment.append(etComment.getText());
            });
        }

        class MyEmojiViewHolder extends RecyclerView.ViewHolder {
            TextView tvEmoji;


            public MyEmojiViewHolder(@NonNull View itemView) {
                super(itemView);
                tvEmoji = itemView.findViewById(R.id.tvEmoji_Comment);
            }
        }
    }
}