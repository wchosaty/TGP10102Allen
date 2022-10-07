package idv.tgp10102.allen;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class JsonCallable implements Callable<String> {
    private static final String TAG = "Tag_JsonCallable";
    private final String url,outJson;

    public JsonCallable(String url, String outJson) {
        this.url = url;
        this.outJson = outJson;
    }

    @Override
    public String call() throws Exception {
        return getJsonData();
    }

    private String getJsonData() {
        HttpURLConnection connection = null;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(0);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("content-type","application/json");
            connection.setRequestProperty("charset","UTF-8");
            try(
                    BufferedWriter bw =
                            new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                    ){
                bw.write(outJson);
                Log.d(TAG,"OutJson :"+outJson);
            }

            int responseCode = connection.getResponseCode();
            if(responseCode == 200){
                try(
                        BufferedReader br =
                                new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        ){
                    String line;
                    while((line = br.readLine()) != null){
                        stringBuilder.append(line);
                    }
                }
            }

        } catch (IOException e) {
            Log.e(TAG,e.toString());
        }finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        Log.d(TAG,"responseReadBack :" + stringBuilder);
        return stringBuilder.toString();
    }


}
