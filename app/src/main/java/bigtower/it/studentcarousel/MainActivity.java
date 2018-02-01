package bigtower.it.studentcarousel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

public class MainActivity extends AppCompatActivity {

    private AsyncHttpClient mAsyncHttpClient;
    RelativeLayout rl;
    String base64 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rl = (RelativeLayout) findViewById(R.id.rl);
        final EditText ip = (EditText)findViewById(R.id.editText);
        Button btn = (Button)findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectServer(ip.getText().toString());

            }
        });

    }

    void connectServer(String ip){
        mAsyncHttpClient = AsyncHttpClient.getDefaultInstance();

        mAsyncHttpClient.websocket("ws://" + ip +":5000/", null, new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"Connected", Toast.LENGTH_SHORT).show();
                    }
                });

                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"Disconnected", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override
                    public void onStringAvailable(final String s) {
                        Log.d("CLIENTTAG",s);
                        Log.d("CLIENT","msg " + s);
                        if(s.equals("start")){
                            base64 = "";
                        }else if(s.equals("end")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    createImageView(base64);
                                }
                            });
                        }else{
                            base64 += s;
                        }
                    }
                });
                webSocket.setDataCallback(new DataCallback() {
                    @Override
                    public void onDataAvailable(DataEmitter emitter, final ByteBufferList bb) {
                        bb.recycle();
                        Log.d("CLIENT","msg byte " + bb.getBytes(bb.size()));

                    }
                });
            }

            private void createImageView(String base64) {
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bmp= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                ImageView image=new ImageView(MainActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                image.setLayoutParams(lp);
                image.setImageBitmap(bmp);
                image.setAdjustViewBounds(true);
                rl.removeAllViews();
                rl.addView(image);
            }

            ;
        });
    }

}
