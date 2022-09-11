package idv.tgp10102.allen;


import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.storage.FirebaseStorage;

import java.util.concurrent.Callable;

public class ImageCallable implements Callable<Boolean> {
    private static final String TAG = "Tag ImageCallable";
    private String path;
    private ImageView imageView;
    final int MEGABYTE = 10 * 1024 * 1024;
    private Context context;

    public ImageCallable(String path, ImageView imageView) {
        this.path = path;
        this.imageView = imageView;
    }

    @Override
    public Boolean call() throws Exception {
        return getStorage();
    }

    private Boolean getStorage() {
        FirebaseStorage.getInstance().getReference().child(this.path)
                .getBytes(MEGABYTE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null){
                        byte[] bytes = task.getResult();
                        imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                    }else {
                        Log.e(TAG, " viewPager2 : downloadStrage Fail");
                    }
                });
        return true;
    }
}
