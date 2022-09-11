package idv.tgp10102.allen;

import android.widget.ImageView;
import android.widget.TextView;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class AccessCallable {
    private static final String TAG = "Tag_AccessCallable";

    public void getViewPicture(String path, ExecutorService executor, ImageView imageView) {
        ImageCallable imageCallable = new ImageCallable(path, imageView);
        Future<Boolean> future = executor.submit(imageCallable);
    }
    public void getCloudThumb(String nickname, String photoName,ImageView imageView, TextView textView,
                              ExecutorService executor, int code) {
        StringCallable stringCallable = new StringCallable(nickname,photoName,imageView,textView,code);
        Future<Boolean> futureCloudThumb = executor.submit(stringCallable);
    }
    public void getCloudComment(String nickname, String photoName,ImageView imageView, TextView textView,
                                ExecutorService executor, int code) {
        StringCallable stringCallable = new StringCallable(nickname,photoName,imageView,textView,code);
        Future<Boolean> futureCloudComment = executor.submit(stringCallable);
    }

}
