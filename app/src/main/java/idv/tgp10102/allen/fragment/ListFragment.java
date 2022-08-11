package idv.tgp10102.allen.fragment;



import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import idv.tgp10102.allen.ComMethod;
import idv.tgp10102.allen.Member;
import idv.tgp10102.allen.R;

public class ListFragment extends Fragment {
    private static final String TAG = "Tag_ListFragment";
    private Activity activity;
    private RecyclerView recyclerView;
    private List<Member> memberObjectsList;
    private SearchView searchView;
    private EditText edUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    private void load() {
        ComMethod.getMemberStringList(activity);


    }

    private void findViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.searchView_List);
        edUser = view.findViewById(R.id.etUser_List);

    }

    private void handleView() {
        memberObjectsList = ComMethod.getMemberObjectsList(activity);
        if(! Objects.equals(memberObjectsList,null) ){
            recyclerView.setAdapter(new MyAdapter(activity, memberObjectsList) );
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        }


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

    static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
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
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

    }
}