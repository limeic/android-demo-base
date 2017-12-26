package tech.lemoncloud.demo;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import tech.lemoncloud.BaseConnectionListener;
import tech.lemoncloud.BaseMessageSendListener;
import tech.lemoncloud.ELemonErrorCode;
import tech.lemoncloud.LemonService;
import tech.lemoncloud.event.BaseEventHandler;
import tech.lemoncloud.event.TextNotifyEventHandler;
import tech.lemoncloud.exception.BaseLemonException;
import tech.lemoncloud.protocol.LemonCommandTypes;
import tech.lemoncloud.protocol.LemonGateway;
import tech.lemoncloud.server.base.net.pdu.LemonBasePackage;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button buttonStart;
    private Button buttonStop;
    private Button buttonTestPing;
    private Button buttonBindUserId;
    private Button buttonTransitHttpGetMsg;
    private Button buttonTransitHttpPostMsg;

    private String url;
    private String demoHttpDomain;
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
        buttonTransitHttpGetMsg = findViewById(R.id.btnTransitHttpGetMsgSend);
        buttonTransitHttpPostMsg = findViewById(R.id.btnTransitHttpPostMsgSend);
    }

    private void initValues() {
        //url = "http://192.168.199.136:3101/route";
        url = "http://lb.lemoncloud.tech/route";
        demoHttpDomain = "http://demo.http.lemoncloud.tech";
        appId = "54927e37f4a053b8c6de537ab7cdd479";
        userId = "123456789";
    }

    /**
     * 处理视图事件
     */
    private void initEvents() throws BaseLemonException {

        // 注册广播处理事件
        LemonService.getInstance().onNotify(new OnMsgNotify());

        // 启动服务
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    LemonService.getInstance().start();
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
        buttonTransitHttpGetMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = "{\"hello\": \"world\", \"age\": 11}";
                String url = null;
                try {
                    url = demoHttpDomain + "/ping?data=" + URLEncoder.encode(data, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                HashMap<String, String> header = new HashMap<>();
                header.put("Content-Type", "application/json;charset=UTF-8");
                LemonService.getInstance().sendHttpMessage(
                        url,
                        "",
                        LemonCommandTypes.HttpMethod.HTTP_METHOD_GET,
                        header,
                        new OnHttpSendListener(new OnHttpEventResp()));
            }
        });

        buttonTransitHttpPostMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = "{\"hello\": \"world\", \"age\": 11}";
                String url = demoHttpDomain + "/ping";
                HashMap<String, String> header = new HashMap<>();
                header.put("Content-Type", "application/json;charset=UTF-8");
                LemonService.getInstance().sendHttpMessage(
                        url,
                        data,
                        LemonCommandTypes.HttpMethod.HTTP_METHOD_POST,
                        header,
                        new OnHttpSendListener(new OnHttpEventResp()));
            }
        });


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

    /**
     * 处理http消息发送
     */
    private class OnHttpSendListener extends BaseMessageSendListener {

        public OnHttpSendListener(BaseEventHandler handler) {
            super(handler);
        }

        @Override
        public void onFail(final ELemonErrorCode eLemonErrorCode) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String msg = "发送Http消息失败. code: " + eLemonErrorCode;
                    Toast.makeText(MainActivity.this, msg, msg.length()).show();
                }
            });
        }

        @Override
        public void onSendComplete() {

        }
    }

    /**
     * 处理http消息回包
     */
    private class OnHttpEventResp extends BaseEventHandler {

        @Override
        public LemonBasePackage handleMessage(LemonBasePackage lemonBasePackage) throws Exception {
            final LemonGateway.LemonPduTransitHttpMsgResp resp = LemonGateway.LemonPduTransitHttpMsgResp.parseFrom(lemonBasePackage.getBody());
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String msg = "http返回数据:  " + resp;
                    Toast.makeText(MainActivity.this, msg, msg.length()).show();
                }
            });
            return null;
        }
    }

    private class OnMsgNotify extends TextNotifyEventHandler {

        @Override
        public void handleMsg(final String content) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String msg = "收到广播消息 " + content;
                    Toast.makeText(MainActivity.this, msg, msg.length()).show();
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        initViews();
        initValues();

        try {
            LemonService.getInstance().init(url, appId, listener);
            initEvents();
        } catch (BaseLemonException e) {
            e.printStackTrace();
        }


    }

}
