package idv.tgp10102.allen.fragment;

import static idv.tgp10102.allen.MainActivity.*;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import idv.tgp10102.allen.AccessCallable;
import idv.tgp10102.allen.Member;
import idv.tgp10102.allen.R;
import idv.tgp10102.allen.User;

public class CloudListFragment extends Fragment {
    private static final String TAG = "Tag_CloudListFragment";
    private Activity activity;
    private RecyclerView recyclerView;
    private List<User> cloudNicknamePersonList;
    private SearchView searchView;
    private ImageButton ibSync;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private ExecutorService executorPicture;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        int numProcess =  Runtime.getRuntime().availableProcessors();
        Log.d(TAG, "JVM可用的處理器數量: " + numProcess);
        // 建立固定量的執行緒放入執行緒池內並重複利用它們來執行任務
        executorPicture = Executors.newFixedThreadPool(numProcess);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cloud_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        cloudNicknamePersonList = new ArrayList<>();
        findViews(view);
        handleViews();

        ibSync.setOnClickListener(v -> {
            load();
        });
    }

    private void load() {
        // 讀取帳戶
        db.collection(getString(R.string.app_name)+"users")
                .get().addOnCompleteListener(taskCloudDB -> {
                    if (taskCloudDB.isSuccessful() && taskCloudDB.getResult() != null) {
                        cloudNicknamePersonList = new ArrayList<>();

                        for(QueryDocumentSnapshot documentSnapshot : taskCloudDB.getResult() ){
                            User userTemp = documentSnapshot.toObject(User.class);
                            Log.d(TAG , "nickname : " +userTemp.getNickName());
                            cloudNicknamePersonList.add(userTemp);
                        }

                        MyCloudListAdapter myCloudListAdapter = (MyCloudListAdapter) recyclerView.getAdapter();
                        myCloudListAdapter.setMyCloudListAdapter(cloudNicknamePersonList);
                        myCloudListAdapter.notifyDataSetChanged();
                        Log.d(TAG , "cloudNicknamePersonList : " + cloudNicknamePersonList.size());

                    } else {
                        Log.e(TAG, "Firebase DB : Download Fail");
                    }
                });
    }


    @Override
    public void onStart() {
        super.onStart();
        activity.findViewById(R.id.cloudListFragment).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.mitList).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.leaderboardFragment).setVisibility(View.VISIBLE);
        load();
    }

    private void handleViews() {
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(new MyCloudListAdapter(activity, cloudNicknamePersonList));
    }

    private void findViews(View view) {
        searchView = view.findViewById(R.id.searchView_CloudList);
        recyclerView = view.findViewById(R.id.recyclerView_CloudList);

        ibSync = view.findViewById(R.id.ibSync_CloudList);
    }

    class MyCloudListAdapter extends RecyclerView.Adapter<MyCloudListAdapter.MyCloudListViewHolder> {
        Context context;
        List<User> list;

        public void setMyCloudListAdapter(List<User> list) {
            this.list = list;
        }

        public MyCloudListAdapter(Context context, List<User> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getItemCount() {
            return list == null ? 0:list.size();
        }

        @NonNull
        @Override
        public MyCloudListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View itemView = layoutInflater.inflate(R.layout.item_cloudnickname,parent,false);
            return new MyCloudListViewHolder(itemView);
        }

        class MyCloudListViewHolder extends RecyclerView.ViewHolder {
            ImageView ivPerson,ivPhoto1,ivPhoto2;
            TextView tvNickname;


            public MyCloudListViewHolder(@NonNull View itemView) {
                super(itemView);
                ivPerson = itemView.findViewById(R.id.ivNickname_itemCloud);
                tvNickname = itemView.findViewById(R.id.tvNickname_itemCloud);
                ivPhoto1 = itemView.findViewById(R.id.ivPhoto1_CloudList);
                ivPhoto2 = itemView.findViewById(R.id.ivPhoto2_CloudList);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull MyCloudListViewHolder holder, int position) {
            Log.d(TAG,"onBindViewHolder : create list(size) :" + list.size());
            holder.tvNickname.setText(list.get(position).getNickName());
            holder.ivPerson.setImageResource(R.drawable.baseline_account_circle_white_48);
            // 下載圖示
            new AccessCallable().getViewPicture(getString(R.string.app_name)+"/userPicture/"+list.get(position).getUid()
                    ,executorPicture,holder.ivPerson);
            // 下載相簿
            FirebaseFirestore.getInstance().collection(getString(R.string.app_name)).document(list.get(position).getNickName())
                    .collection(list.get(position).getNickName()).get().addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            if(task.getResult() != null && task.getResult().size()>0){
                                QuerySnapshot snapshot = task.getResult();
                                Member member1 = snapshot.getDocuments().get(0).toObject(Member.class);
                                new AccessCallable().getViewPicture(member1.getCloudChildPhotosPathList().get(0)
                                        ,executorPicture,holder.ivPhoto1);
                                if(task.getResult().size() >= 2){
                                    Member member2 = snapshot.getDocuments().get(1).toObject(Member.class);
                                    new AccessCallable().getViewPicture(member2.getCloudChildPhotosPathList().get(1)
                                            ,executorPicture,holder.ivPhoto2);
                                }
                            }

                        }
                    });

            holder.itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString(NAME,list.get(position).getNickName());
                Log.d(TAG,"list.get(position) : "+list.get(position) );
                Navigation.findNavController(v).navigate(R.id.action_cloudListFragment_to_mitDetail,bundle);
            });
        }
    }

}