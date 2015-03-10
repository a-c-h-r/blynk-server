package cc.blynk.server.core.ssl;

import cc.blynk.common.stats.GlobalStats;
import cc.blynk.common.utils.ServerProperties;
import cc.blynk.server.core.BaseHandlersHolder;
import cc.blynk.server.core.BaseServer;
import cc.blynk.server.dao.FileManager;
import cc.blynk.server.dao.SessionsHolder;
import cc.blynk.server.dao.UserRegistry;
import cc.blynk.server.handlers.workflow.BaseSimpleChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.List;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 2/1/2015.
 */
public class SSLAppServer extends BaseServer {

    private final BaseHandlersHolder handlersHolder;
    private final ChannelInitializer<SocketChannel> channelInitializer;

    public SSLAppServer(ServerProperties props, FileManager fileManager, UserRegistry userRegistry, SessionsHolder sessionsHolder, GlobalStats stats) {
        super(props.getIntProperty("server.ssl.port"), props);

        this.handlersHolder = new SSLAppHandlersHolder(props, fileManager, userRegistry, sessionsHolder);

        this.channelInitializer = new SSLAppChannelInitializer(sessionsHolder, stats, handlersHolder,
                initSslContext(
                        new File(props.getProperty("server.ssl.cert")),
                        new File(props.getProperty("server.ssl.key")),
                        props.getProperty("server.ssl.key.pass"))
        );

        log.info("SSL server port {}.", port);
    }

    public static SslContext initSslContext(File serverCert, File serverKey, String keyPass) {
        try {
            //todo this is self-signed cerf. just to simplify for now for testing.
            return SslContext.newServerContext(serverCert, serverKey, keyPass);
        } catch (SSLException e) {
            log.error("Error initializing ssl context. Reason : {}", e.getMessage());
            System.exit(0);
           //todo throw?
        }
        return null;
    }

    @Override
    public List<BaseSimpleChannelInboundHandler> getBaseHandlers() {
        return handlersHolder.getBaseHandlers();
    }

    @Override
    public ChannelInitializer<SocketChannel> getChannelInitializer() {
        return channelInitializer;
    }

    @Override
    public void stop() {
        log.info("Shutting down SSL server...");
        super.stop();
    }

}
