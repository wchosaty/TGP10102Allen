package idv.tgp10102.allen;

import android.content.Context;
import android.widget.ImageView;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import idv.tgp10102.allen.ImageCallable;

public class AccessCallable {
    private static final String TAG = "Tag_AccessCallable";

    public void getViewPicture(String path, ExecutorService executor, ImageView imageView) {
        ImageCallable imageCallable = new ImageCallable(path, imageView);
        Future<Boolean> future = executor.submit(imageCallable);
    }
}
