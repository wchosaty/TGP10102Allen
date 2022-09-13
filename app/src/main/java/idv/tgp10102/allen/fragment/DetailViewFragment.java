package idv.tgp10102.allen.fragment;


import static idv.tgp10102.allen.MainActivity.NAME;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import idv.tgp10102.allen.AccessCallable;
import idv.tgp10102.allen.MainActivity;
import idv.tgp10102.allen.Member;
import idv.tgp10102.allen.R;
import idv.tgp10102.allen.User;

public class DetailViewFragment extends Fragment {
    private static final String TAG = "Tag DetailViewFragment";
    private Activity activity;
    private ImageView ivCurrentPhotoNick;
    private TextView tvCurrentPhotoNick;
    private RecyclerView recyclerViewDetail;
    private List<Member> currentCloudMemberList;
    public static List<String> selectPhotosPathList;
    private String currentPhotoNickname;
    private ImageButton ibBack;
    private ExecutorService executorPicture,executorCloudContent;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        int processNumber = Runtime.getRuntime().availableProcessors();
        Log.d(TAG, "JVM可用的處理器數量: " + processNumber);
        // 建立固定量的執行緒放入執行緒池內並重複利用它們來執行任務
        executorCloudContent = executorPicture = Executors.newFixedThreadPool(processNumber/2);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        currentCloudMemberList = new ArrayList<>();
        findViews(view);
        handleView();

    }

    @Override
    public void onStart() {
        super.onStart();
        activity.findViewById(R.id.cloudListFragment).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.mitList).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.leaderboardFragment).setVisibility(View.INVISIBLE);
        // 取Bundle Request
        if (getArguments() != null) {
            String bundleRequest = getArguments().getString(NAME);
            // 接收update要求
            if (bundleRequest != null) {
                Log.d(TAG, "bundleRequest : " + bundleRequest);
                ivCurrentPhotoNick.setImageResource(R.drawable.baseline_account_circle_white_24);
                currentPhotoNickname = bundleRequest;
                if (currentPhotoNickname != null) {
                    // 取得該相簿的Nickname 圖示

                    db.collection(getString(R.string.app_name) + "users").get().addOnCompleteListener(taskNick -> {
                        if (taskNick.isSuccessful() && taskNick.getResult() != null) {
                            String nickUid;
                            Log.d(TAG, "taskUserData : Successful");
                            for (QueryDocumentSnapshot snapshot : taskNick.getResult()) {
                                User userNick = snapshot.toObject(User.class);
                                if (Objects.equals(userNick.getNickName(), currentPhotoNickname)) {
                                    // 新增流量計算
                                    Map<String, Object> mapCount = new HashMap<>();

                                    mapCount.put("user", MainActivity.CURRENTNICKNAME);
                                    String s = String.valueOf(System.currentTimeMillis());
                                    mapCount.put("id", s);
                                    FirebaseFirestore.getInstance().collection(getString(R.string.app_name)).document("CountRQ").collection(userNick.getNickName())
                                            .document(s).set(mapCount);
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("name", currentPhotoNickname);
                                    FirebaseFirestore.getInstance().collection(getString(R.string.app_name)).document("CountRQ").collection("List")
                                            .document(currentPhotoNickname).set(data);

                                    // 下載圖像
                                    nickUid = userNick.getUid();
                                    final int MEGABYTE = 10 * 1024 * 1024;
                                    storage.getReference(getString(R.string.app_name) + "/userPicture/" + nickUid)
                                            .getBytes(MEGABYTE).addOnCompleteListener(taskNickPic -> {
                                                Log.d(TAG, "taskNickPic : Successful");
                                                byte[] bytes = taskNickPic.getResult();
                                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                ivCurrentPhotoNick.setImageBitmap(bitmap);
                                            });
                                    break;
                                } else {
                                    Log.d(TAG, "taskNickPic : Fail");
                                }
                            }
                        }
                    });
                }
                tvCurrentPhotoNick.setText(bundleRequest);
                downloadPhotosList();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(executorPicture != null){
            executorPicture.shutdownNow();
        }
        if(executorCloudContent != null){
            executorCloudContent.shutdownNow();
        }
    }

    private void downloadPhotosList() {
        db.collection(getString(R.string.app_name)).document(currentPhotoNickname)
                .collection(currentPhotoNickname).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (currentCloudMemberList.size() > 0) {
                            currentCloudMemberList.clear();
                        }
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            currentCloudMemberList.add(document.toObject(Member.class));
                        }
                        Log.d(TAG, "QueryDocumentSnapshot :" + currentCloudMemberList.size());
                        MyDetailAdapter cloudDetailAdapter = (MyDetailAdapter) recyclerViewDetail.getAdapter();
                        cloudDetailAdapter.setMyDetailAdapter(currentCloudMemberList);
                        cloudDetailAdapter.notifyDataSetChanged();

                    } else {
                        Log.e(TAG, "Firebase : Download Fail");
                    }
                });
    }

    private void handleView() {
        recyclerViewDetail.setAdapter(new MyDetailAdapter(activity, currentCloudMemberList));
        recyclerViewDetail.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL));

        selectPhotosPathList = new ArrayList<>();
    }

    private void findViews(View view) {
        recyclerViewDetail = view.findViewById(R.id.recyclerView_detail);
        ibBack = view.findViewById(R.id.ibBack_Edit);
        tvCurrentPhotoNick = view.findViewById(R.id.tvCurrentPhotoNick_Main);
        ivCurrentPhotoNick = view.findViewById(R.id.ivCurrentPhotoNick_Detail);
    }


    class MyDetailAdapter extends RecyclerView.Adapter<MyDetailAdapter.DetailViewHolder> {
        private Context context;
        List<Member> list;

        public MyDetailAdapter(Context context, List<Member> list) {
            this.context = context;
            this.list = list;

        }

        public void setMyDetailAdapter(List<Member> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public DetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View itemView = layoutInflater.inflate(R.layout.item_detail,parent,false);

            return new DetailViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull DetailViewHolder holder, int position) {
            final Member member = list.get(position);
            holder.nameDetail.setText(member.getStringName());
            holder.contentDetail.setText(member.getStringMessage());
            Log.d(TAG,"picture List.size :" + list.size() +" / " +position );
            holder.recyclerPicture.setAdapter(new MyPictureAdapter(context,member.getCloudChildPhotosPathList()) );
            holder.recyclerPicture.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL));

            holder.ivThumbCloud.setVisibility(View.INVISIBLE);
            holder.tvThumbCloud.setVisibility(View.INVISIBLE);
            new AccessCallable().getCloudThumb(member.getNickname(),member.getStringName(), holder.ivThumbCloud,holder.tvThumbCloud,executorCloudContent,1);
            new AccessCallable().getCloudComment(member.getNickname(),member.getStringName(), holder.ivContentCloud,holder.tvContentCloud,executorCloudContent,2);
            // 按讚
            holder.ivThumbSet.setOnClickListener(v -> {
                Map<String, Object> data = new HashMap<>();
                data.put("user",MainActivity.CURRENTNICKNAME);
                FirebaseFirestore.getInstance().collection(getString(R.string.app_name)).document("ThumbRQ").collection("ThumbRQ")
                .document(member.getNickname()).collection(member.getStringName()).document(MainActivity.CURRENTNICKNAME).set(data).addOnCompleteListener(
                        task -> {
                            if(task.isSuccessful()){
                                new AccessCallable().getCloudThumb(member.getNickname(),member.getStringName(),
                                        holder.ivThumbCloud,holder.tvThumbCloud,executorCloudContent,1);
                            }
                        });
            });

            // 留言
            holder.ivContentSet.setOnClickListener(v -> {
                onClickDetail(member,v);
            });
            holder.ivContentCloud.setOnClickListener(v -> {
                onClickDetail(member,v);
            });
            holder.tvContentCloud.setOnClickListener(v -> {
                onClickDetail(member,v);
            });
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        class DetailViewHolder extends RecyclerView.ViewHolder {
            TextView nameDetail,contentDetail;
            TextView tvThumbCloud,tvContentCloud;
            RecyclerView recyclerPicture;
            ImageView ivThumbSet,ivThumbCloud,ivContentSet,ivContentCloud;

            public DetailViewHolder(@NonNull View itemView) {
                super(itemView);
                nameDetail = itemView.findViewById(R.id.tvName_itemDetail);
                contentDetail = itemView.findViewById(R.id.tvContent_itemDetail);
                recyclerPicture = itemView.findViewById(R.id.recyclerPicture);
                tvThumbCloud = itemView.findViewById(R.id.tvThumbCloud);
                tvContentCloud = itemView.findViewById(R.id.tvContentCloud);

                ivThumbSet = itemView.findViewById(R.id.ivThumbSet);
                ivThumbCloud = itemView.findViewById(R.id.ivThumbCloud);
                ivContentSet = itemView.findViewById(R.id.ivContentSet);
                ivContentCloud =itemView.findViewById(R.id.ivContentCloud);
            }
        }
    }
    public void onClickDetail(Member member,View v) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("member",member);
        Navigation.findNavController(v).navigate(R.id.action_mitDetail_to_editCommentFragment,bundle);

    }

    // recyclerView picture
    class MyPictureAdapter extends RecyclerView.Adapter<MyPictureAdapter.PictureViewHolder> {
        private Context context;
        private List<String> list;

        public MyPictureAdapter(Context context,List<String> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public PictureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View itemView = layoutInflater.inflate(R.layout.fragment_add_photos,parent,false);
            return new PictureViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull PictureViewHolder holder, int position) {
            new AccessCallable().getViewPicture(list.get(position),executorPicture,holder.ivAddPhoto);
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        class PictureViewHolder extends RecyclerView.ViewHolder {
            ImageView ivAddPhoto;

            public PictureViewHolder(@NonNull View itemView) {
                super(itemView);
                ivAddPhoto = itemView.findViewById(R.id.ivAddPhoto);
            }
        }
    }

}