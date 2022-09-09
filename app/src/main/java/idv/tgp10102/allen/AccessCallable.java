package idv.tgp10102.allen;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import idv.tgp10102.allen.ImageCallable;
import idv.tgp10102.allen.fragment.StringCallable;

public class AccessCallable {
    private static final String TAG = "Tag_AccessCallable";

    public void getViewPicture(String path, ExecutorService executor, ImageView imageView) {
        ImageCallable imageCallable = new ImageCallable(path, imageView);
        Future<Boolean> future = executor.submit(imageCallable);
    }
    public void getCloudContent(String nickname, String photoName,ImageView imageView, TextView textView, ExecutorService executor, int code) {
        StringCallable stringCallable = new StringCallable(nickname,photoName,imageView,textView,code);
        Future<Boolean> futureCloudContent = executor.submit(stringCallable);
    }
//    public void setCloudContent(String path, ExecutorService executor) {
//        StringCallable s = new StringCallable();
//        Future<Boolean> future = executor.submit();
//    }

}
