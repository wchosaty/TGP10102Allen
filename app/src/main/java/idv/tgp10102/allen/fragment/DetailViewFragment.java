package idv.tgp10102.allen.fragment;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import idv.tgp10102.allen.ComMethod;
import idv.tgp10102.allen.R;

public class DetailViewFragment extends Fragment {
    private static final String TAG = "Tag DetailViewFragment";
    private Activity activity;
    private static TextView tvMessage;
    private static EditText nameDetail;
    private RecyclerView recyclerViewDetail;
    private List<Member> detailObjectsList;
    public static List<String> selectPhotosPathList;
    private ImageView ivBigPhoto;
    private TextView ivBigText;
    public static Integer touchNumber = -1;
    public static Boolean touchFlag = false;

    private static MyDetailPager2 myDetailPager2;
    private static ViewPager2 detailPager2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        findViews(view);
        load();
        handleView();
        handleImageViewToBigPic();
        handleBigPicToImagerView();
    }

    private void handleView() {
        detailObjectsList = ComMethod.getMemberObjectsList(activity);
        recyclerViewDetail.setAdapter(new MyDetailAdapter(activity, detailObjectsList));
        recyclerViewDetail.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL));

        selectPhotosPathList = new ArrayList<>();
//        myDetailPager2 = new MyDetailPager2(this,selectPhotosPathList);
        myDetailPager2 = new MyDetailPager2((FragmentActivity) activity,selectPhotosPathList);
        detailPager2.setAdapter(myDetailPager2);

    }

    public void updateDetail(Member member){
        nameDetail.setText(member.getStringName());

    }

    private void findViews(View view) {
        tvMessage = view.findViewById(R.id.tvMessage_detail);
        nameDetail = view.findViewById(R.id.Name_detail);

        ConstraintLayout constraintLayout = view.findViewById(R.id.conLayoutBigView);
        recyclerViewDetail = view.findViewById(R.id.recyclerView_detail);
        ivBigPhoto = view.findViewById(R.id.ivBigCrop);
        ivBigText = view.findViewById(R.id.tvBigMessage);

        detailPager2 = view.findViewById(R.id.viewPager2_detail);
    }

    private void handleBigPicToImagerView() {
        ivBigPhoto.setOnClickListener(v -> {
            ivBigPhoto.setVisibility(View.INVISIBLE);
            ivBigText.setVisibility(View.INVISIBLE);
        });

        ivBigText.setOnClickListener(v -> {
            ivBigPhoto.setVisibility(View.INVISIBLE);
            ivBigText.setVisibility(View.INVISIBLE);
        });
    }

    private void handleImageViewToBigPic() {

    }

    private void onButtonBigViewClick(View view,int code){
    }

    private void load() {
        ivBigPhoto.setVisibility(View.INVISIBLE);
        ivBigText.setVisibility(View.INVISIBLE);
        ComMethod.getMemberStringList(activity);
    }

    static class MyDetailAdapter extends RecyclerView.Adapter<MyDetailAdapter.DetailViewHolder> {
        private Context context;
        List<Member> list;

        public MyDetailAdapter(Context context, List<Member> list) {
            this.context = context;
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
            Member member = list.get(position);

            Bitmap bitmap = null;
            File filePicPath = null;
            StringBuilder s;


            holder.nameDetail.setText(member.getStringName());
            try {
                filePicPath = new File(member.getMyPhotosPashList().get(0).toString() );
                holder.ivPicDetail.setImageBitmap( ComMethod.bitmapToImageFilePath(bitmap,filePicPath) );
            } catch (IOException e) {
                e.printStackTrace();
            }

            holder.itemView.setOnClickListener(v -> {
                if(member != null){

                    nameDetail.setText(member.getStringName());
                    tvMessage.setText(member.getStringMessage());
                    selectPhotosPathList = member.getMyPhotosPashList();
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

//        public MyDetailPager2(@NonNull Fragment fragment, List<String> list) {
//            super(fragment);
//            this.list = list;
//        }

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