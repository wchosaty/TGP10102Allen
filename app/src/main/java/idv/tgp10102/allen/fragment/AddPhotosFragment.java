package idv.tgp10102.allen.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.Objects;

import idv.tgp10102.allen.R;

public class AddPhotosFragment extends Fragment {
    private static final String TAG = "Tag AddPhotosFragment";
    private Activity activity;
    private ImageView ivAddPhoto;
    private String photoPath;
    private FirebaseStorage storage;

    public AddPhotosFragment(String photoPath) {
        this.photoPath = photoPath;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_photos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        findViews(view);
        updatePhotosView();
    }

    private void updatePhotosView() {
        if (Objects.equals(this.photoPath, null)) {
            return;
        }

        ivAddPhoto.setImageResource(R.drawable.baseline_add_black_48);
        File filePicPath = new File(this.photoPath.toString());
 //       Bitmap bitmap = null;
        //設定預設圖片尺寸
        if(Objects.equals(photoPath,"-1")){
            ViewGroup.LayoutParams params = ivAddPhoto.getLayoutParams();
            params.height = 300;
            params.width = 300;
            ivAddPhoto.setLayoutParams(params);
        }

        // cloud修改路徑
        if(!Objects.equals(this.photoPath,null)){
            final int MEGABYTE = 4 * 1024 * 1024;
                String imagePath = this.photoPath;
                StorageReference imageRef = storage.getReference().child(imagePath);
                imageRef.getBytes(MEGABYTE)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null){
                                byte[] bytes = task.getResult();
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                ivAddPhoto.setImageBitmap(bitmap);
                            }else {
                                Log.e(TAG, "onBindViewHolder : downloadStrage Fail");
                            }
                        });
        }

    }

    private void findViews(View view) {

        ivAddPhoto = view.findViewById(R.id.ivAddPhoto);
    }

}