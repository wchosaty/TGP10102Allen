package idv.tgp10102.allen.fragment;

import static idv.tgp10102.allen.MainActivity.*;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import idv.tgp10102.allen.ComMethod;
import idv.tgp10102.allen.MainActivity;
import idv.tgp10102.allen.Member;
import idv.tgp10102.allen.R;

public class ListFragment extends Fragment {
    private static final String TAG = "Tag_ListFragment";
    private Activity activity;
    private RecyclerView recyclerView;
    private List<Member> memberObjectsList;
    private SearchView searchView;
    private ImageButton ibUploadList,ibDeleteList;
    private File myDir_list;
    private List<String> currentMyList;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    public boolean[] itemChoose;
    public Map<String, String> itemChooseMap;
    private boolean displayCheckBox = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        memberObjectsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        myDir_list = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        currentMyList = new ArrayList<>();
        load();
        findViews(view);
        handleView();
        handleButton();

        // Add to Edit
        view.findViewById(R.id.btFloatingAdd).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(WORKTYPE,CREATENEW);
            Navigation.findNavController(v).navigate(R.id.action_mitList_to_mitEdit,bundle);
        });

    }

    private void handleButton() {
        //勾選刪除
        ibDeleteList.setOnClickListener(v -> {
            if (displayCheckBox) {
                MyAdapter adapter = (MyAdapter) recyclerView.getAdapter();
                List<Member> list = adapter.getAdapterMembers();
                if (list.size() == itemChoose.length) {
                    for (int i = 0; i < list.size(); i++) {

                        if (itemChoose[i]) {
                            Member member = list.get(i);
                            Log.d(TAG, "itemChoose " + i + ":" + itemChoose[i]);
                            deletePhotoToCloudFromList(member);

                     }
                    }

                    displayCheckBox = false;
                    adapter.setAdapterMembers(memberObjectsList);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        // 勾選上傳
        ibUploadList.setOnClickListener(v -> {
            if (displayCheckBox) {
                MyAdapter adapter = (MyAdapter) recyclerView.getAdapter();
                List<Member> list = adapter.getAdapterMembers();
                if (list.size() == itemChoose.length) {
                    for (int i = 0; i < list.size(); i++) {
                        if (itemChoose[i]) {
                            Member member = list.get(i);
                            if(Objects.equals(null,member)){
                                Log.d(TAG,"ibUploadList null");
                            }
                            uploadPhotoToCloudFromList(member);
                        }
                    }
                    displayCheckBox = false;
                    adapter.setAdapterMembers(memberObjectsList);
                    adapter.notifyDataSetChanged();
                }
            }
        });

    }

    private void deletePhotoToCloudFromList(Member member) {
        String photoName = member.getStringName();
            Log.d(TAG,"member.getNickname(): "+member.getNickname());
            Log.d(TAG,"member.getStringName(): "+member.getStringName());

            db.collection(getString(R.string.app_name))
                    .document(member.getNickname())
                    .collection(member.getNickname())
                    .document(member.getStringName())
                    .get().addOnCompleteListener(task -> {
                        Member memberCloud = task.getResult().toObject(Member.class);

                        for(int j=0;j < memberCloud.getCloudChildPhotosPathList().size() ;j++){
                            storage.getReference().child(memberCloud.getCloudChildPhotosPathList().get(j))
                                    .delete().addOnCompleteListener(taskStorage -> {
                                        if(taskStorage.isSuccessful()){
                                            Log.d(TAG,"delete :taskStorage.isSuccessful()");
                                        }else{
                                            Log.d(TAG,"delete :taskStorage Fail.");
                                        }
                                    });
                            db.collection(getString(R.string.app_name)).document(memberCloud.getNickname())
                                    .collection(memberCloud.getNickname()).document(memberCloud.getStringName())
                                    .delete()
                                    .addOnCompleteListener(taskDB -> {
                                        if (taskDB.isSuccessful()) {
                                            Log.d(TAG, "delete :taskDB.isSuccessful()");
                                        } else {
                                            Log.d(TAG, "delete :taskDB Fail");
                                        }
                                    });
                        }

                    });


    }

    private void uploadPhotoToCloudFromList(Member member) {
        if(member ==null){
            Toast.makeText(activity, R.string.file_nodata, Toast.LENGTH_SHORT).show();
            return ;
        }

        File file = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        List<String> cloudList = new ArrayList<>();
        for(int i=0; i<member.getLocalChildPathList().size();i++){

            File childFile = new File(file, member.getLocalPhotoParentPath()+"/"+member.getLocalChildPathList().get(i));
            Uri sourceUri = Uri.fromFile(childFile);
            String imagePath = getString(R.string.app_name) + member.getCloudPhotosParentPath()+"/"+member.getLocalChildPathList().get(i);
            Log.d(TAG,"Storage imagePath : "+imagePath);
            cloudList.add(imagePath);
            storage.getReference().child(imagePath).putFile(sourceUri)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "putFile : isSuccessful");
                        } else {
                            Log.d(TAG, "putFile : fail");
                        }
                    });
        }
        member.setCloudChildPhotosPathList(cloudList);
        //要修改Nickname
        db.collection(getString(R.string.app_name)).document(CURRENTNICKNAME)
                .collection(CURRENTNICKNAME)
                .document(member.getStringName().toString())
                .set(member)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d(TAG,"upload DB : isSuccessful");
                    }else {
                        Log.d(TAG,"upload DB : fail");
                    }
                });
    }

    private Member loadMemberFromList(String string) {
        try(
                FileInputStream fis = activity.openFileInput(string);
                ObjectInputStream ois = new ObjectInputStream(fis);
        )
        {
            return (Member) ois.readObject();
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        displayCheckBox = false;
        load();
        activity.findViewById(R.id.cloudListFragment).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.mitList).setVisibility(View.VISIBLE);

    }

    private void load() {
        File f = new File(myDir_list.toString()+"/"+LOCALNICKNAME);
        if(!f.exists()){
            return;
        }
        File[] files= f.listFiles();
        if(files.length>0){
            for (int i = 0; i < files.length; i++) {
                currentMyList.add(String.valueOf(new StringBuilder(files[i].getName().trim())));
            }
        }
    }

    private void findViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.searchView_CloudList);

        ibUploadList = view.findViewById(R.id.ibUpload_List);
        ibDeleteList = view.findViewById(R.id.ibDelete_List);
    }


    private List<Member> getMyListToObjectsList() {
        List<Member> list = new ArrayList<>();
        if(Objects.equals(currentMyList,null) ||
                currentMyList.size() < 0){
            return list;
        }
        for (int i = 0; i < currentMyList.size(); i++) {

            String sbTemp = String.valueOf(currentMyList.get(i));
            try(
                    FileInputStream fis = activity.openFileInput(sbTemp);
                    ObjectInputStream ois = new ObjectInputStream(fis);
            )
            {
                Member member = (Member) ois.readObject();
                list.add(member);
            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG, Arrays.toString(e.getStackTrace()));
            }
        }
        return list;
    }

    private void handleView() {
//        memberObjectsList = ComMethod.getMemberObjectsList(activity);
        memberObjectsList = getMyListToObjectsList();

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

        public List<Member> getAdapterMembers() {
            return this.list;
        }

        public MyAdapter(Context context, List<Member> list) {
            this.context = context;
            this.list = list;
            itemChoose = new boolean[list.size()];

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
            CheckBox checkBox;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName_item);
                ivPic1 = itemView.findViewById(R.id.iv1_item);
                checkBox = itemView.findViewById(R.id.cbChoose);

            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Log.d(TAG,"MainActivity.remoteCould : "+ MainActivity.remoteCould);
                final Member member = list.get(position);
                Bitmap bitmap = null;
                File filePicPath = null;
                StringBuilder s;

                try {
                    filePicPath = new File(member.getLocalPhotosPathList().get(0).toString() );
                    holder.ivPic1.setImageBitmap( ComMethod.bitmapToImageFilePath(bitmap,filePicPath) );

                } catch (IOException e) {
                    e.printStackTrace();
                }
                holder.tvName.setText(member.getStringName());
                //List選擇
                if(displayCheckBox){
                    holder.checkBox.setVisibility(View.VISIBLE);

                }else{
                    holder.checkBox.setVisibility(View.INVISIBLE);
                }

                //挑選 勾選item
                holder.checkBox.setOnClickListener(v -> {
                    itemChoose[position] = holder.checkBox.isChecked();
                    Log.d(TAG,"itemChoose : index[ "+ position +" ]- " + itemChoose[position] );
                    // select To Upload cloud
                    if(itemChoose[position]){
//                        itemChooseMap.put(member.getStringName(),String.valueOf(position) );
                    }else {
//                        itemChooseMap.remove(member.getStringName());
                    }

                });

                holder.itemView.setOnClickListener(v -> {
                    // ToUpdateMember
                    if(!displayCheckBox){
                        Bundle bundle = new Bundle();
                        bundle.putString(WORKTYPE,UPDATE);
                        bundle.putString(NAME,member.getStringName());
                        Navigation.findNavController(v).navigate(R.id.action_mitList_to_mitEdit,bundle);
                    }

                });

                holder.itemView.setOnLongClickListener(v -> {
                    displayCheckBox = !displayCheckBox;
                    this.notifyDataSetChanged();
                    return true;
                });

        }

    }

}