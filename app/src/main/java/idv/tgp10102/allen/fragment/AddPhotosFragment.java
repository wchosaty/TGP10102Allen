package idv.tgp10102.allen.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import idv.tgp10102.allen.ComMethod;
import idv.tgp10102.allen.MainActivity;
import idv.tgp10102.allen.R;

public class AddPhotosFragment extends Fragment {
    private Activity activity;
    private ImageView ivAddPhoto;
    private String photoPath;

    public AddPhotosFragment(String photoPath) {
        this.photoPath = photoPath;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        Bitmap bitmap = null;
        //設定預設圖片尺寸
        if(Objects.equals(photoPath,"-1")){
            ViewGroup.LayoutParams params = ivAddPhoto.getLayoutParams();
            params.height = 300;
            params.width = 300;
            ivAddPhoto.setLayoutParams(params);
        }

        try {
            ivAddPhoto.setImageBitmap(ComMethod.bitmapToImageFilePath(bitmap, filePicPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void findViews(View view) {

        ivAddPhoto = view.findViewById(R.id.ivAddPhoto);
    }

}