package idv.tgp10102.allen.fragment;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import idv.tgp10102.allen.MainActivity;
import idv.tgp10102.allen.Member;
import idv.tgp10102.allen.R;
import idv.tgp10102.allen.User;
import idv.tgp10102.allen.UserList;

public class EditCommentFragment extends Fragment {
    private static final String TAG = "Tag EditCommentFragment";
    private Activity activity;
    private EditText etComment;
    private ImageView ivSend;
    private Member member;
    private RecyclerView recyclerViewComment,recyclerViewEmoji;
    private Map<String,Object> mapEmoji;
    private List<Map<String,Object>> commentDataList;
    private List<User> userList;
    private StringBuilder sbAddEmojiToComment;
    private ExecutorService executorCommentNickPicture;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int processNumber =  Runtime.getRuntime().availableProcessors();
        Log.d(TAG, "JVM可用的處理器數量: " + processNumber);
        // 建立固定量的執行緒放入執行緒池內並重複利用它們來執行任務
        executorCommentNickPicture = Executors.newFixedThreadPool(processNumber/2);
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
        userList = new ArrayList<>();
        commentDataList = new ArrayList<>();
        findViews(view);
        handleViews();
    }

    private void handleViews() {
        recyclerViewComment.setLayoutManager(new LinearLayoutManager(activity));
        MyCommentAdapter myCommentAdapter = new MyCommentAdapter(activity,commentDataList);
        recyclerViewComment.setAdapter(myCommentAdapter);
        recyclerViewEmoji.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL));
        MyEmojiAdapter myEmojiAdapter = new MyEmojiAdapter(activity,mapEmoji);
        recyclerViewEmoji.setAdapter(myEmojiAdapter);

        // 送出留言...
        ivSend.setOnClickListener(v -> {
            Map<String,Object> mapData = new HashMap<>();
            mapData.put("user",MainActivity.CURRENTNICKNAME);
            mapData.put("comment",etComment.getText().toString());
            if (etComment.getText() != null && etComment.getText().toString() != "") {
                FirebaseFirestore.getInstance().collection(getString(R.string.app_name)).document("CommentRQ").collection("CommentRQ")
                        .document(member.getNickname()).collection(member.getStringName()).document(String.valueOf(System.currentTimeMillis()))
                        .set(mapData).addOnCompleteListener(taskSendComment -> {
                                    if (taskSendComment.isSuccessful()) {
                                        Log.d(TAG,"taskSendComment : isSuccessful");
                                        etComment.setText("");
                                        LoadComment();
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
        loadUserList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(executorCommentNickPicture != null){
            executorCommentNickPicture.shutdownNow();
        }
    }

    private void loadUserList() {
        userList = new ArrayList<>();
        FirebaseFirestore.getInstance().collection(getString(R.string.app_name)+"users")
                .get().addOnCompleteListener(taskCloudDB -> {
                    if (taskCloudDB.isSuccessful() && taskCloudDB.getResult() != null) {
                        for(QueryDocumentSnapshot documentSnapshot : taskCloudDB.getResult() ){
                            User userTemp = documentSnapshot.toObject(User.class);
                            Log.d(TAG , "nickname : " +userTemp.getNickName());
                            userList.add(userTemp);
                        }
                    LoadComment();
                    } else {
                        Log.e(TAG, "Firebase DB : Download Fail");
                    }
                });
    }

    private void LoadComment() {
        commentDataList = new ArrayList<>();
        FirebaseFirestore.getInstance().collection(getString(R.string.app_name)).document("CommentRQ")
                .collection("CommentRQ").document(member.getNickname()).collection(member.getStringName())
                .get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Map<String,Object> mapTemp = new HashMap<>();
                        Log.d(TAG,"LoadComment size : " + task.getResult().size());
                        for (int i=0;i<task.getResult().size();i++) {
                            QueryDocumentSnapshot queryDocumentSnapshot = (QueryDocumentSnapshot) task.getResult().getDocuments().get(i);
                            mapTemp = queryDocumentSnapshot.getData();
                            commentDataList.add(mapTemp);
                        }
                        Log.d(TAG,"LoadComment /commentObjectList :"+commentDataList.size());
                        MyCommentAdapter adapter = (MyCommentAdapter) recyclerViewComment.getAdapter();
                        adapter.setList(commentDataList);
                        adapter.notifyDataSetChanged();
                    }

                });
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

    // Comment
    class MyCommentAdapter extends RecyclerView.Adapter<MyCommentAdapter.MyCommentViewHolder> {
        Context context;
        List<Map<String,Object>> list;

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        public void setList(List<Map<String,Object>> list) {
            this.list = list;
        }

        public MyCommentAdapter(Context context,List<Map<String,Object>> list) {
            this.context = context;
            this.list = list;
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
            holder.tvNickname.setText((String) list.get(position).get("user") );
            holder.tvComment.setText((String) list.get(position).get("comment") );
            new UserList((String) list.get(position).get("user"),executorCommentNickPicture,holder.ivNickPicture);

        }

        class MyCommentViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivNickPicture;
            private TextView tvNickname,tvComment;

            public MyCommentViewHolder(@NonNull View itemView) {
                super(itemView);
                ivNickPicture = itemView.findViewById(R.id.ivNickPicture_Comment);
                tvNickname = itemView.findViewById(R.id.tvNickname_Comment);
                tvComment = itemView.findViewById(R.id.tvComment_Comment);
            }
        }
    }

    // Emoji
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
                char charPos = (char) (97 +position);
                String sPos = Character.toString(charPos);
                Log.d(TAG,"onBindViewHolder : "+ s + " / " +position);
                sbAddEmojiToComment.append((String) map.get(sPos));
                etComment.setText(sbAddEmojiToComment.toString());
                sbAddEmojiToComment = new StringBuilder();
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