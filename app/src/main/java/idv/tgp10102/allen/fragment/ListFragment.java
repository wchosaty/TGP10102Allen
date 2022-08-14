package idv.tgp10102.allen.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import idv.tgp10102.allen.ComMethod;
import idv.tgp10102.allen.MainActivity;
import idv.tgp10102.allen.Member;
import idv.tgp10102.allen.R;

public class ListFragment extends Fragment {
    private static final String TAG = "Tag_ListFragment";
    private Activity activity;
    private RecyclerView recyclerView;
    private List<Member> memberObjectsList = new ArrayList<>();
    private SearchView searchView;
    private EditText etUser;
    private Button button;
    private ImageButton ivShareList;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        load();
        findViews(view);
        handleView();
        handleButton();
    }

    private void handleButton() {
        //測試remote
        MainActivity.remoteCould = true;
        etUser.setText(MainActivity.remoteCould.toString());

        button.setOnClickListener(v -> {
//            MainActivity.remoteCould = !MainActivity.remoteCould;

        });
        //測試下載Firebase
        ivShareList.setOnClickListener(v -> {
            if(MainActivity.remoteCould){
                 StringBuilder dbPath = new StringBuilder();

                db.collection(getString(R.string.app_name)+MainActivity.LOCALNICKNAME).get()
                        .addOnCompleteListener(task -> {
                            Log.d(TAG,"db.collection : start /PATH :"+String.valueOf(dbPath));
                            if(task.isSuccessful() && task.getResult() != null){

                                ComMethod.currentCloudList = new ArrayList<>();
                                for(QueryDocumentSnapshot document : task.getResult()){
                                    ComMethod.currentCloudList.add(document.toObject(Member.class));
                                }
                                Log.d(TAG,"QueryDocumentSnapshot :"+ComMethod.currentCloudList.size());
                                MyAdapter cloudAdapter = (MyAdapter) recyclerView.getAdapter();
                                cloudAdapter.setAdapterMembers(ComMethod.currentCloudList);
                                cloudAdapter.notifyDataSetChanged();
//                                handleCloudData();

                            }else{
                                Log.e(TAG, "Firebase : Download Fail");
                            }
                        });
            }
        });

    }

//    private void handleCloudData() {
//       MyAdapter cloudAdapter = (MyAdapter) recyclerView.getAdapter();
//       cloudAdapter.setAdapterMembers(ComMethod.currentCloudList);
//       cloudAdapter.notifyDataSetChanged();
//    }


    private void load() {
        ComMethod.getMemberStringList(activity);


    }

    private void findViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.searchView_List);
        etUser = view.findViewById(R.id.etUser_List);

        ivShareList = view.findViewById(R.id.ivShare_List);
        button = view.findViewById(R.id.button);

    }

    private void handleView() {
        memberObjectsList = ComMethod.getMemberObjectsList(activity);
//        if(! Objects.equals(memberObjectsList,null) ){
//            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
//            recyclerView.setAdapter(new MyAdapter(activity, memberObjectsList) );
//
//        }

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(new MyAdapter(activity, memberObjectsList) );


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                MyAdapter adapter =(MyAdapter) recyclerView.getAdapter();
                if(adapter != null){
                    // 如果搜尋條件為空字串，就顯示原始資料；否則就顯示搜尋後結果
                    if(newText.isEmpty()){
                        adapter.setAdapterMembers(memberObjectsList);
                    }else {
                        List<Member> searchList = new ArrayList<>();
                        for(Member member : memberObjectsList){
                            if(member.getStringName().toString().toUpperCase().contains(newText.toUpperCase())) {
                                searchList.add(member);
                            }
                        }
                        adapter.setAdapterMembers(searchList);
                    }
                    adapter.notifyDataSetChanged();
                    return true;
                }

                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        Context context;
        List<Member> list;

        public void setAdapterMembers(List<Member> list) {
            this.list = list;
        }

        public MyAdapter(Context context, List<Member> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View itemView = layoutInflater.inflate(R.layout.item_view, parent,false);
            return new ViewHolder(itemView);
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            ImageView ivPic1;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName_item);
                ivPic1 = itemView.findViewById(R.id.iv1_item);

            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Log.d(TAG,"onBindViewHolder : BuildStart1");
            Log.d(TAG,"MainActivity.remoteCould : "+ MainActivity.remoteCould);
            if(MainActivity.remoteCould){
                final Member member = ComMethod.currentCloudList.get(position);
                Log.d(TAG,"member.getCloudChildPhotosPathList().size(): "+member.getCloudChildPhotosPathList().size());
                if(member.getCloudChildPhotosPathList().size() >= 0){
                    final int MEGABYTE = 2 * 1024 * 1024;
 //                   downloadStrage(holder.ivPic1, member.getCloudChildPhotosPathList());
                    etUser.setText(MainActivity.remoteCould.toString());
                    Log.d(TAG,"UploadPath : BuildStart2");
                    Log.d(TAG,"UploadPath : "+member.getCloudChildPhotosPathList().get(0));
                    for(int i=0;i<member.getCloudChildPhotosPathList().size();i++){

                        String imagePath = member.getCloudChildPhotosPathList().get(i);

                        StorageReference imageRef = storage.getReference().child(imagePath);
                        imageRef.getBytes(MEGABYTE)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult() != null){
                                        byte[] bytes = task.getResult();
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        holder.ivPic1.setImageBitmap(bitmap);
                                    }else {
                                        Log.e(TAG, "onBindViewHolder : downloadStrage Fail");
                                    }
                                });
                    }

                }

                holder.tvName.setText(member.getStringName());

            }else{
                final Member member = list.get(position);
                Bitmap bitmap = null;
                File filePicPath = null;
                StringBuilder s;
                EditFragment.currentEditList = new ArrayList<>();


                try {
                    filePicPath = new File(member.getLocalPhotosPathList().get(0).toString() );
                    holder.ivPic1.setImageBitmap( ComMethod.bitmapToImageFilePath(bitmap,filePicPath) );

                } catch (IOException e) {
                    e.printStackTrace();
                }
                holder.tvName.setText(member.getStringName());
            }

        }

    }

    private void downloadStrage(final ImageView ivPic, List<String> cloudChildPhotosPathList) {
        final int MEGABYTE = 2 * 1024 * 1024;

    }
}