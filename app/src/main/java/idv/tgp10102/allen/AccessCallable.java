package idv.tgp10102.allen;

import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class AccessCallable {
    private static final String TAG = "Tag_AccessCallable";
    public static String SERVER_URL = "XXX";


    public void getViewPicture(String path, ExecutorService executor, ImageView imageView) {
        ImageCallable imageCallable = new ImageCallable(path, imageView);
        Future<Boolean> future = executor.submit(imageCallable);
    }
    public void getCloudThumb(String nickname, String photoName,ImageView imageView, TextView textView,
                              ExecutorService executor, int code) {
        StringCallable stringCallable = new StringCallable(nickname,photoName,imageView,textView,code);
        Future<Boolean> futureCloudThumb = executor.submit(stringCallable);
    }
    public void getCloudComment(String nickName, String photoName,ImageView imageView, TextView textView,
                                ExecutorService executor, int code) {
        StringCallable stringCallable = new StringCallable(nickName,photoName,imageView,textView,code);
        Future<Boolean> futureCloudComment = executor.submit(stringCallable);
    }
    public void getJsonData(String url, String jsonString, ExecutorService executor){

        JsonCallable jsonCallable = new JsonCallable(url,jsonString);
        Future<String> futureJsonDate = executor.submit(jsonCallable);
    }

}
