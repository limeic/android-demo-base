package tech.lemoncloud.demo;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import tech.lemoncloud.BaseConnectionListener;
import tech.lemoncloud.BaseMessageSendListener;
import tech.lemoncloud.ELemonErrorCode;
import tech.lemoncloud.LemonService;
import tech.lemoncloud.event.BaseEventHandler;
import tech.lemoncloud.net.pdu.LemonBasePackage;
import tech.lemoncloud.protocol.LemonGateway;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button buttonStart;
    private Button buttonStop;
    private Button buttonTestPing;
    private Button buttonBindUserId;

    private String url;
    private String appId;
    private String userId;

    /**
     * 定义connection listener
     */
    private BaseConnectionListener listener = new BaseConnectionListener() {

        @Override
        public void onRegistry() {
            // Tips: 此处为网络线程的回调，如需与UI交互，需要切换到主线程
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String msg = "服务已启动";
                    Toast.makeText(MainActivity.this, msg, msg.length()).show();
                }
            });
        }

        @Override
        public void onClosed() {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String msg = "服务已断开";
                    Toast.makeText(MainActivity.this, msg, msg.length()).show();
                }
            });
        }

        @Override
        public void onStopped() {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String msg = "服务已停止";
                    Toast.makeText(MainActivity.this, msg, msg.length()).show();
                }
            });
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
        //url = "http://192.168.199.136:3101/route";
        url = "http://lb.test.lemoncloud.tech/route";
        appId = "54927e37f4a053b8c6de537ab7cdd479";
        userId = "123456789";
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
                // 发送ping消息
                LemonService.getInstance().ping(new OnPingSendListener(new OnPingEventResp()));
            }
        });

        // 绑定用户ID
        buttonBindUserId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 测试绑定用户ID
                LemonService.getInstance().bindUserId(userId, new BaseEventHandler() {
                    @Override
                    public LemonBasePackage handleMessage(LemonBasePackage lemonBasePackage) throws Exception {
                        final LemonGateway.LemonPduBindUserResp resp = LemonGateway.LemonPduBindUserResp.parseFrom(lemonBasePackage.getBody());
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                String msg = "绑定用户返回结果. resp: " + resp;
                                Toast.makeText(MainActivity.this, msg, msg.length()).show();
                            }
                        });
                        return null;
                    }
                });
            }
        });

        // 发送透传Http消息

    }

    /**
     * 绑定用户发送回调
     */
    private class OnPingSendListener extends BaseMessageSendListener {

        public OnPingSendListener(BaseEventHandler handler) {
            super(handler);
        }

        @Override
        public void onFail(final ELemonErrorCode eLemonErrorCode) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String msg = "发送Ping包失败. code: " + eLemonErrorCode;
                    Toast.makeText(MainActivity.this, msg, msg.length()).show();
                }
            });
        }

        @Override
        public void onSendComplete() {
            Log.d(TAG, "send ping to lemoncloud complete.");
        }
    }

    /**
     * bind user回包事件处理
     */
    private class OnPingEventResp extends BaseEventHandler {

        @Override
        public LemonBasePackage handleMessage(LemonBasePackage lemonBasePackage) throws Exception {
            final LemonGateway.LemonPduPingResp resp = LemonGateway.LemonPduPingResp.parseFrom(lemonBasePackage.getBody());
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String msg = "发送Ping结果. resp: " + resp;
                    Toast.makeText(MainActivity.this, msg, msg.length()).show();
                }
            });
            return null;
        }
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
