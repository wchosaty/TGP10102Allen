package idv.tgp10102.allen.fragment;


import static idv.tgp10102.allen.MainActivity.NAME;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import idv.tgp10102.allen.AccessCallable;
import idv.tgp10102.allen.ComMethod;
import idv.tgp10102.allen.LoginActivity;
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
    private ExecutorService executorPicture;
    public ProgressDialog mProgressDialog;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        int numProcess = Runtime.getRuntime().availableProcessors();
        Log.d(TAG, "JVM可用的處理器數量: " + numProcess);
        // 建立固定量的執行緒放入執行緒池內並重複利用它們來執行任務
        executorPicture = Executors.newFixedThreadPool(numProcess/2);
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


        // 取Bundle Request
        if (getArguments() != null) {
            String bundleRequest = getArguments().getString(NAME);
            // 接收update要求
            if (bundleRequest != null) {
                Log.d(TAG,"bundleRequest : "+ bundleRequest);
                ivCurrentPhotoNick.setImageResource(R.drawable.baseline_account_circle_white_24);
                currentPhotoNickname = bundleRequest;
                if(currentPhotoNickname != null){
                    // 取得該相簿的Nickname 圖示

                    db.collection(getString(R.string.app_name)+"users").get().addOnCompleteListener(taskNick -> {
                        if (taskNick.isSuccessful() && taskNick.getResult() != null) {
                            String nickUid;
                            Log.d(TAG, "taskUserData : Successful");
                            for (QueryDocumentSnapshot snapshot : taskNick.getResult()) {
                                User userNick = snapshot.toObject(User.class);
                                if (Objects.equals(userNick.getNickName(), currentPhotoNickname)) {
                                    // 新增流量計算
                                    Map<String, Object> mapCount = new HashMap<>();

                                    mapCount.put("user",MainActivity.CURRENTNICKNAME);
                                    String s = String.valueOf(System.currentTimeMillis());
                                    mapCount.put("id", s);
                                    FirebaseFirestore.getInstance().collection(getString(R.string.app_name)).document("CountRQ").collection(userNick.getNickName())
                                            .document(s).set(mapCount);
                                    Map<String, Object> data = new HashMap<>();
                                     data.put("name",currentPhotoNickname);
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
    public void onStart() {
        super.onStart();
        activity.findViewById(R.id.cloudListFragment).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.mitList).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.leaderboardFragment).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(executorPicture != null){
            executorPicture.shutdownNow();
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

    protected void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(requireContext());
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.getProgress();
            mProgressDialog.setMessage("Loading...");
        }

        mProgressDialog.show();
    }

    protected void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    class MyDetailAdapter extends RecyclerView.Adapter<MyDetailAdapter.DetailViewHolder> {
        private Context context;
        List<Member> list;
        boolean[] booleanThumb;

        public MyDetailAdapter(Context context, List<Member> list) {
            this.context = context;
            this.list = list;
//            booleanThumb = new boolean[this.list.size()];

        }

        public void setMyDetailAdapter(List<Member> list) {
            this.list = list;
            booleanThumb = new boolean[this.list.size()];
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

            if(booleanThumb.length > 0){
                holder.ivThumbUp.setVisibility(booleanThumb[position] ? View.VISIBLE :View.INVISIBLE);
            }

            holder.itemView.setOnClickListener(v -> {
                booleanThumb[position] = true;
                holder.ivThumbUp.setVisibility(View.VISIBLE);
//                this.notifyItemChanged(position);

                Map<String, Object> data = new HashMap<>();
                data.put("nickname",member.getNickname());
                data.put("photo",member.getStringName());
                data.put("user",MainActivity.CURRENTNICKNAME);
                String s = String.valueOf(System.currentTimeMillis());
                data.put("id",s);
                FirebaseFirestore.getInstance().collection(getString(R.string.app_name)).document("ThumbRQ").collection("ThumbRQ")
                        .document(s).set(data);
            });

        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        class DetailViewHolder extends RecyclerView.ViewHolder {
            TextView nameDetail;
            TextView contentDetail;
            RecyclerView recyclerPicture;
            ImageView ivThumbUp;

            public DetailViewHolder(@NonNull View itemView) {
                super(itemView);
                nameDetail = itemView.findViewById(R.id.tvName_itemDetail);
                contentDetail = itemView.findViewById(R.id.tvContent_itemDetail);
                recyclerPicture = itemView.findViewById(R.id.recyclerPicture);
                ivThumbUp = itemView.findViewById(R.id.ivThumbUp);
            }
        }
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

//            showProgressDialog();

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