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
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import idv.tgp10102.allen.ComMethod;
import idv.tgp10102.allen.Member;
import idv.tgp10102.allen.R;

public class DetailViewFragment extends Fragment {
    private static final String TAG = "Tag DetailViewFragment";
    private Activity activity;
    private static TextView tvMessage;
    private static EditText nameDetail;
    private ImageView ivCurrentPhotoNick;
    private TextView tvCurrentPhotoNick;
    private RecyclerView recyclerViewDetail;
    private List<Member> detailObjectsList;
    private List<Member> currentCloudMemberList;
    public static List<String> selectPhotosPathList;
    private String currentPhotoNickname;
    private ImageButton ibBack;
    private static MyDetailPager2 myDetailPager2;
    private static ViewPager2 detailPager2;

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
            String bundleRequest = getArguments().getString("Nickname");
            // 接收update要求
            if (bundleRequest != null) {
                tvMessage.setText(bundleRequest);
                }
            }
    }

    @Override
    public void onStart() {
        super.onStart();
        activity.findViewById(R.id.cloudListFragment).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.mitList).setVisibility(View.INVISIBLE);
    }

    private void downloadPhotosList() {
                        db.collection(getString(R.string.app_name)).document(currentPhotoNickname)
                                .collection(currentPhotoNickname).get()
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful() && task.getResult() != null){
                                if(currentCloudMemberList.size() > 0){
                                    currentCloudMemberList.clear();
                                }
                                for(QueryDocumentSnapshot document : task.getResult()){
                                    currentCloudMemberList.add(document.toObject(Member.class));
                                }
                                Log.d(TAG,"QueryDocumentSnapshot :"+currentCloudMemberList.size());
                                MyDetailAdapter cloudDetailAdapter = (MyDetailAdapter) recyclerViewDetail.getAdapter();
                                cloudDetailAdapter.setMyDetailAdapter(currentCloudMemberList);
                                cloudDetailAdapter.notifyDataSetChanged();

                            }else{
                                Log.e(TAG, "Firebase : Download Fail");
                            }
                        });
    }

    private void handleView() {
        detailObjectsList = ComMethod.getMemberObjectsList(activity);
        recyclerViewDetail.setAdapter(new MyDetailAdapter(activity, currentCloudMemberList));
        recyclerViewDetail.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL));

        selectPhotosPathList = new ArrayList<>();
        myDetailPager2 = new MyDetailPager2((FragmentActivity) activity,selectPhotosPathList);
        detailPager2.setAdapter(myDetailPager2);

        ibBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_mitDetail_to_cloudListFragment);
        });

    }

    private void findViews(View view) {
        tvMessage = view.findViewById(R.id.tvMessage_detail);
        nameDetail = view.findViewById(R.id.Name_detail);

        recyclerViewDetail = view.findViewById(R.id.recyclerView_detail);

        detailPager2 = view.findViewById(R.id.viewPager2_detail);
        ibBack = view.findViewById(R.id.ibBack_Edit);

        tvCurrentPhotoNick = view.findViewById(R.id.tvCurrentPhotoNick_Detail);
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

            File filePicPath = null;
            StringBuilder s;

            holder.nameDetail.setText(member.getStringName());
            if(member.getCloudChildPhotosPathList().size() > 0){
                final int MEGABYTE = 2 * 1024 * 1024;
                for(int i=0;i<member.getCloudChildPhotosPathList().size();i++){
                    String imagePath = member.getCloudChildPhotosPathList().get(i);
                    StorageReference imageRef = storage.getReference().child(imagePath);
                    imageRef.getBytes(MEGABYTE)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult() != null){
                                    byte[] bytes = task.getResult();
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    holder.ivPicDetail.setImageBitmap(bitmap);
                                }else {
                                    Log.e(TAG, "onBindViewHolder : downloadStrage Fail");
                                }
                            });
                    }
            }

            // viewPager2
            holder.itemView.setOnClickListener(v -> {
                if(member != null){

                    nameDetail.setText(member.getStringName());
                    tvMessage.setText(member.getStringMessage());
                    selectPhotosPathList= new ArrayList<>();

                    // cloud修改
                    //selectPhotosPathList=member.getLocalPhotosPathList();
                    selectPhotosPathList=member.getCloudChildPhotosPathList();

                    myDetailPager2.setMyDetailPager2Adapter(selectPhotosPathList);
                    detailPager2.setAdapter(myDetailPager2);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        class DetailViewHolder extends RecyclerView.ViewHolder {
            TextView nameDetail;
            ImageView ivPicDetail;

            public DetailViewHolder(@NonNull View itemView) {
                super(itemView);
                nameDetail = itemView.findViewById(R.id.tvName_itemDetail);
                ivPicDetail = itemView.findViewById(R.id.iv_itemDetail);
            }
        }
    }
    static class MyDetailPager2 extends FragmentStateAdapter {
        private List<String> list;

        public MyDetailPager2(@NonNull FragmentActivity fragmentActivity, List<String> list) {
            super(fragmentActivity);
            this.list = list;
        }

        public void setMyDetailPager2Adapter(List<String> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            String s = new String(list.get(position));
            return new AddPhotosFragment(s);
        }

        @Override
        public int getItemCount() {
            return list == null ? 0: list.size();
        }
    }

}