package tech.lemoncloud.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import tech.lemoncloud.sdk.client.BaseConnectionListener;
import tech.lemoncloud.sdk.client.LemonService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button buttonStart;
    private Button buttonStop;
    private Button buttonTestPing;
    private Button buttonBindUserId;

    private String url;
    private String appId;

    /**
     * 定义connection listener
     */
    private BaseConnectionListener listener = new BaseConnectionListener() {
        @Override
        public void onConnected() {
            Log.d(TAG, "connected!");
        }

        @Override
        public void onClosed() {
            Log.d(TAG, "closed");
        }

        @Override
        public void onStopped() {
            Log.d(TAG, "stopped");
        }
    };

    /**
     * 绑定视图
     */
    private void initViews() {
        buttonStart = findViewById(R.id.btnStartService);
        buttonStop = findViewById(R.id.btnStopService);
        buttonTestPing = findViewById(R.id.btnTestPing);
        buttonBindUserId = findViewById(R.id.btnBindUserId);
    }

    private void initValues() {
        url = "http://lb.test.lemoncloud.tech/route";
        appId = "123456790";
    }

    /**
     * 处理视图事件
     */
    private void initEvents() {

        // 启动服务
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    LemonService.getInstance().start(url, appId, listener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // 关闭服务
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LemonService.getInstance().stop();
            }
        });

        // 测试ping消息
        buttonTestPing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: 发送ping消息
            }
        });

        // 绑定用户ID
        buttonBindUserId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: 测试绑定用户ID
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        initValues();

        initEvents();
    }

}
