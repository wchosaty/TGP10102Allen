package idv.tgp10102.allen;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.concurrent.Callable;

public class RecycCallable implements Callable<Bitmap> {
    private String path;
    private Bitmap bitmap;
    private ImageView imageView;
    private byte[] bytes;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    final int MEGABYTE = 10 * 1024 * 1024;

    public RecycCallable(String path, ImageView imageView) {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        this.path = path;
        this.imageView = imageView;
    }

    @Override
    public Bitmap call() throws Exception {
        return getStroage();
    }

    private Bitmap getStroage (){
        storage.getReference(path).getBytes(MEGABYTE)
                .addOnCompleteListener(taskStroage -> {
                    if (taskStroage.isSuccessful() && taskStroage.getResult() != null){
                        byte[] bytes = taskStroage.getResult();
                        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageView.setImageBitmap(bitmap);
                    }else {
                        Log.e("RecycCallable", "onBindViewHolder : downloadStorage Fail");
                    }
                });
        return  bitmap;
    }
}
