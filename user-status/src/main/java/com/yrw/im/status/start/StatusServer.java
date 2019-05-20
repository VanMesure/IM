package com.yrw.im.status.start;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yrw.im.common.code.MsgDecoder;
import com.yrw.im.common.code.MsgEncoder;
import com.yrw.im.status.handler.StatusServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Date: 2019-05-20
 * Time: 18:45
 *
 * @author yrw
 */
public class StatusServer {
    private static final Logger logger = LoggerFactory.getLogger(StatusServer.class);

    private static Injector injector = Guice.createInjector();

    static void start(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap()
            .group(bossGroup, workGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast("MsgDecoder", injector.getInstance(MsgDecoder.class));
                    pipeline.addLast("MsgEncoder", new MsgEncoder());
                    pipeline.addLast("StatusServerHandler", injector.getInstance(StatusServerHandler.class));
                }
            });

        bootstrap.bind(new InetSocketAddress(port)).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                //TODO: do some init
                logger.info("[IM status] start successful, waiting for clients connecting......");
            } else {
                logger.error("[IM status] start failed!");
            }
        });
    }
}