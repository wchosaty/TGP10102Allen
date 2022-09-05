package idv.tgp10102.allen.fragment;

import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.storage.FirebaseStorage;

import java.util.concurrent.Callable;

public class ViewPagerCallable implements Callable<Boolean> {
    private static final String TAG = "Tag ViewPagerCallable";
    private String path;
    private ImageView imageView;
    private FirebaseStorage storage;
    final int MEGABYTE = 10 * 1024 * 1024;

    public ViewPagerCallable(String path, ImageView imageView) {
        storage = FirebaseStorage.getInstance();
        this.path = path;
        this.imageView = imageView;
        this.storage = storage;
    }

    @Override
    public Boolean call() throws Exception {
        return getStorage();
    }

    private Boolean getStorage() {
                storage.getReference().child(this.path)
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
