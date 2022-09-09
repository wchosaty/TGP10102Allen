package idv.tgp10102.allen.fragment;

import android.app.Activity;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import idv.tgp10102.allen.AccessCallable;
import idv.tgp10102.allen.R;
import idv.tgp10102.allen.SortObject;
import idv.tgp10102.allen.UserList;

public class LeaderboardFragment extends Fragment {
    private static final String TAG ="Tag LeaderboardFragment";
     private Activity activity;
     private TextView tvTile;
    private RecyclerView recyclerView;
    private  List<SortObject> listSort;
    private ExecutorService executorPicture;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listSort = new ArrayList<SortObject>();
        int numProcess =  Runtime.getRuntime().availableProcessors() > 3 ? 3 : Runtime.getRuntime().availableProcessors();
        Log.d(TAG, "JVM可用的處理器數量: " + numProcess);
        // 建立固定量的執行緒放入執行緒池內並重複利用它們來執行任務
        executorPicture = Executors.newFixedThreadPool(numProcess);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        activity = getActivity();
        findViews(view);
        recyclerView.setAdapter(new BoardMyAdapter(activity,listSort));
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL));
    }

    private void findViews(View view) {
        tvTile = view.findViewById(R.id.tvTitle_Board);
        recyclerView = view.findViewById(R.id.recyclerView_Board);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadUserListSort();
    }

    public void loadUserListSort () {
        List<String> list = new ArrayList<>();
        listSort = new ArrayList<>();
        // views times計算
        FirebaseFirestore.getInstance().collection(getString(R.string.app_name)).document("CountRQ").collection("List")
                .get().addOnCompleteListener(task -> {
                    for (int i = 0; i < task.getResult().size(); i++) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(i);
                        list.add((String) document.get("name"));
                        String stringNickname = list.get(i);
                        Log.d(TAG, "temp  : " + stringNickname);
                        FirebaseFirestore.getInstance().collection(getString(R.string.app_name)).document("CountRQ")
                                .collection(stringNickname).get()
                                .addOnCompleteListener(taskSort -> {
                                    Log.d(TAG, "taskSort size : " + taskSort.getResult().size() + " / " + stringNickname);
                                    SortObject sortObject = new SortObject(taskSort.getResult().size(), "" + stringNickname);
                                    Log.d(TAG, "sortObject : " + sortObject.getCount() + " / " + sortObject.getStringContent());
                                    listSort.add(sortObject);
                                    Log.d(TAG, "taskSort : +1");
                                    if (listSort.size() == task.getResult().size()) {
                                        Log.d(TAG, "taskSort : 完成");
                                        Log.d(TAG, "taskSort.getResult().size() : " + taskSort.getResult().size());
                                        Collections.sort(listSort);
                                        BoardMyAdapter adapterTemp = (BoardMyAdapter) recyclerView.getAdapter();
                                        adapterTemp.setAdapter(listSort);
                                        adapterTemp.notifyDataSetChanged();
                                    }
                                });
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(executorPicture != null){
            executorPicture.shutdownNow();
        }
    }

    class BoardMyAdapter extends RecyclerView.Adapter<BoardMyAdapter.BoardViewHolder> {
        Context context;
        List<SortObject> list;

        public void setAdapter( List<SortObject> list) {
            this.list = list;
        }

        public BoardMyAdapter(Context context, List<SortObject> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        @NonNull
        @Override
        public BoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View itemView = layoutInflater.inflate(R.layout.item_leaderboard,parent,false);
            return new BoardViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull BoardViewHolder holder, int position) {
            SortObject sortObject = list.get(position);
            new UserList(sortObject.getStringContent(),executorPicture,holder.ivPicture);
            StringBuilder sb = new StringBuilder();
            sb.append(sortObject.getCount().toString());
            holder.tvTimes.setText(sb.toString()+" " + getString(R.string.views) );
            holder.tvNickname.setText(sortObject.getStringContent());
            holder.tvNumber.setText(String.valueOf(position+1));

        }

        class BoardViewHolder extends RecyclerView.ViewHolder {
            TextView tvNumber,tvTimes,tvNickname;
            ImageView ivPicture;

            public BoardViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNumber = itemView.findViewById(R.id.tvNumber_Board);
                tvTimes = itemView.findViewById(R.id.tvTimes_Board);
                tvNickname = itemView.findViewById(R.id.tvNickName_Board);
                ivPicture = itemView.findViewById(R.id.ivNickPicture_Board);
            }
        }
    }
}