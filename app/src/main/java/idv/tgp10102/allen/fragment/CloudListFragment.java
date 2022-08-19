package idv.tgp10102.allen.fragment;

import static idv.tgp10102.allen.MainActivity.*;
import android.app.Activity;
import android.content.Context;
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
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import idv.tgp10102.allen.R;
import idv.tgp10102.allen.User;

public class CloudListFragment extends Fragment {
    private static final String TAG = "Tag_CloudListFragment";
    private Activity activity;
    private RecyclerView recyclerView;
    private List<String> cloudNicknamePersonList;
    private SearchView searchView;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

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

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
            db.collection(getString(R.string.app_name)+"users")
                    .get().addOnCompleteListener(taskCloudDB -> {
                        if (taskCloudDB.isSuccessful() && taskCloudDB.getResult() != null) {
                            cloudNicknamePersonList = new ArrayList<>();

                            for(QueryDocumentSnapshot documentSnapshot : taskCloudDB.getResult() ){
                                User userTemp = documentSnapshot.toObject(User.class);
                                Log.d(TAG , "nickname : " +userTemp.getNickName());
                                cloudNicknamePersonList.add(userTemp.getNickName());
                            }

                            MyCloudListAdapter myCloudListAdapter = (MyCloudListAdapter) recyclerView.getAdapter();
                            myCloudListAdapter.setMyCloudListAdapter(cloudNicknamePersonList);
                            myCloudListAdapter.notifyDataSetChanged();

                         Log.d(TAG,"DB user documentSnapshot : " + cloudNicknamePersonList.size());
                        } else {
                            Log.e(TAG, "Firebase DB : Download Fail");
                        }
              });
    }

    private void handleViews() {
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(new MyCloudListAdapter(activity, cloudNicknamePersonList));
    }

    private void findViews(View view) {
        searchView = view.findViewById(R.id.searchView_CloudList);
        recyclerView = view.findViewById(R.id.recyclerView_CloudList);
    }

    class MyCloudListAdapter extends RecyclerView.Adapter<MyCloudListAdapter.MyCloudListViewHolder> {
        Context context;
        List<String> list;

        public void setMyCloudListAdapter(List<String> list) {
            this.list = list;
        }

        public MyCloudListAdapter(Context context, List<String> list) {
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
            ImageView ivPerson;
            TextView tvNickname;


            public MyCloudListViewHolder(@NonNull View itemView) {
                super(itemView);
                ivPerson = itemView.findViewById(R.id.ivNickname_itemCloud);
                tvNickname = itemView.findViewById(R.id.tvNickname_itemCloud);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull MyCloudListViewHolder holder, int position) {
            holder.ivPerson.setImageResource(R.drawable.baseline_account_circle_white_48);
            holder.tvNickname.setText(list.get(position));

            holder.itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString(NAME,list.get(position));
                Navigation.findNavController(v).navigate(R.id.action_cloudListFragment_to_mitDetail,bundle);
            });
        }
    }

}