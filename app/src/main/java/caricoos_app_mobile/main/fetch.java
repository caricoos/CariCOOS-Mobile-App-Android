package caricoos_app_mobile.main;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Class to make a HTTP request to fetch data.
 */
public class fetch {

    String url;

    public fetch(String url) {
        this.url = url;
    }

    String postDataException() throws ClientProtocolException, IOException {
        String result = "";
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();
        result = EntityUtils.toString(entity);

        return result;
    }

    String postData() {
        String result = "";
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        try {
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity);

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        return result;
    }


}
