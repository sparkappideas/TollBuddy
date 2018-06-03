package in.spark.idea.tollbuddy;

import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * Created by Saba on 30/7/16.
 */
public class MyWebChromeClient extends WebChromeClient {
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
    }


}
