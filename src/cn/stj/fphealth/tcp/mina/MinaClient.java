
package cn.stj.fphealth.tcp.mina;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import android.content.Context;
import android.content.Intent;
import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.app.FPHealthApplication;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.util.NetworkUtil;
import cn.stj.fphealth.util.PreferencesUtils;

public class MinaClient {

    private SocketConnector connector;
    private ConnectFuture future;
    private IoSession session;
    private static MinaClient mInstance;
    private static Context mContext;
    private static final String HOST = "120.25.160.36";
    private static final int PORT = 6999;
    private boolean mIsDeviceBind;

    public static MinaClient getInstance(Context context) {
        if (mInstance == null) {
            mContext = context;
            mInstance = new MinaClient();
        }
        return mInstance;
    }

    public void connect() {
        LogUtil.i("debug", "=============MinaClient========connect=============");
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                // 创建一个socket连接
                connector = new NioSocketConnector();
                // 设置链接超时时间
                connector.setConnectTimeoutMillis(PreferencesUtils.getInt(mContext,
                        Constants.DEVICE_PARAM.TIMEOUT, 60) * 1000);
                // 获取过滤器链
                DefaultIoFilterChainBuilder filterChain = connector.getFilterChain();
                filterChain.addFirst("reconnection", new IoFilterAdapter() {
                    @Override
                    public void sessionClosed(NextFilter nextFilter, IoSession ioSession)
                            throws Exception {
                        for (;;) {
                            try {
                                Thread.sleep(3000);
                                ConnectFuture future = connector.connect();
                                future.awaitUninterruptibly();// 等待连接创建成功
                                session = future.getSession();// 获取会话
                                if (session.isConnected()) {
                                    LogUtil.i("debug", "断线重连["
                                            + HOST + ":" + PORT
                                            + "]成功");
                                    break;
                                }
                            } catch (Exception ex) {
                                if (!NetworkUtil.checkNetwork(mContext)) {
                                    Intent intent = new Intent(Constants.HEALTH_RECEIVER_ACTION);
                                    intent.putExtra(Constants.IS_NETWORK_AVAILABLE, false);
                                    FPHealthApplication.getInstance().sendBroadcast(intent);
                                }
                                LogUtil.i("debug", "重连服务器登录失败,3秒再连接一次:" + ex.getMessage());
                            }
                        }
                    }
                });
                // 添加编码过滤器 处理乱码、编码问题
                filterChain.addLast("codec", new ProtocolCodecFilter(new CommandCodecFactory()));
                filterChain.addLast("exceutor", new ExecutorFilter(Executors.newCachedThreadPool()));
                // 消息核心处理器
                connector.setHandler(new ClientMessageHandlerAdapter(mContext));
                connector.getSessionConfig().setReceiveBufferSize(1024 * 1024); // 设置接收缓冲区的大小
                connector.getSessionConfig().setSendBufferSize(1024 * 1024);
                connector.setDefaultRemoteAddress(new InetSocketAddress(HOST, PORT));// 设置默认访问地址
                for (;;) {
                    try {
                        ConnectFuture future = connector.connect();
                        // 等待连接创建成功
                        future.awaitUninterruptibly();
                        // 获取会话
                        session = future.getSession();
                        LogUtil.i("debug", "连接服务端" + HOST + ":" + PORT + "[成功]" + ",,时间:"
                                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                                + "==sesssionId:" + session.getId());

                        break;
                    } catch (Exception e) {
                        if (!NetworkUtil.checkNetwork(mContext)) {
                            Intent intent = new Intent(Constants.HEALTH_RECEIVER_ACTION);
                            intent.putExtra(Constants.IS_NETWORK_AVAILABLE, false);
                            FPHealthApplication.getInstance().sendBroadcast(intent);
                        }
                        LogUtil.i("debug", "连接服务端" + HOST + ":" + PORT + "失败" + ",,时间:"
                                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                                + ", 连接MSG异常,请检查MSG端口、IP是否正确,MSG服务是否启动,异常内容:" + e.getMessage());
                        try {
                            Thread.sleep(5000);// 连接失败后,重连间隔5s
                        } catch (InterruptedException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }).start();

    }

    public void setAttribute(Object key, Object value) {
        if (session != null) {
            session.setAttribute(key, value);
        }
    }

    public void send(TcpProtocol tcpProtocol) {
        if (session != null && session.isConnected()) {
            session.write(tcpProtocol);
        }
    }

    public boolean close() {
        if (isConnect()) {
            session.close();
            return true;
        }
        return false;
    }

    public SocketConnector getConnector() {
        return connector;
    }

    public IoSession getSession() {
        return session;
    }

    public boolean isConnect() {
        return session != null && session.isConnected();
    }
}
