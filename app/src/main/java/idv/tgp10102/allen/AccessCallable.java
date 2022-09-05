package idv.tgp10102.allen;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import androidx.viewpager2.widget.ViewPager2;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import idv.tgp10102.allen.fragment.ViewPagerCallable;

public class AccessCallable {
    private static final String TAG = "Tag_AccessCallable";


    public Bitmap getRecycImage(String path, ExecutorService executor, ImageView imageView) {
        RecycCallable recycCallable = new RecycCallable(path, imageView);
        Future<Bitmap> future = executor.submit(recycCallable);
        Bitmap bitmap = null;
        try {
            bitmap = future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.d(TAG, "AccessCallable : " + e.toString());
        }
        return bitmap;
    }

    public void getViewPicture(String path, ExecutorService executor, ImageView imageView) {
        ViewPagerCallable viewPagerCallable = new ViewPagerCallable(path, imageView);
        Future<Boolean> future = executor.submit(viewPagerCallable);
    }
}
