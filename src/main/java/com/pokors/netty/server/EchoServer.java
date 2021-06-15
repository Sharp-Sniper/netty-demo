package com.pokors.netty.server;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * TODO
 *
 * @author andrew.liuhp@foxmail.com
 * @version V1.0
 * @since 2021/6/15 22:37
 */
public class EchoServer {
    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: " + EchoServer.class.getSimpleName() + " <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        new EchoServer(port).start();
    }

    public void start() throws Exception {
        final EchoServerHandler serverHandler = new EchoServerHandler();
        // 创建EventLoopGroup
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            // 创建ServerBootstrap
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(eventLoopGroup)
                // 指定所使用的NIO传输Channel
                .channel(NioServerSocketChannel.class)
                // 使用指定的端口设置套接字地址
                .localAddress(new InetSocketAddress(port))
                // 添加一个EchoServerHandler到子Channel的ChannelPipeline
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                            // EchoServerHandler被标注为@Shareable，所以我们可以总是使用同样的实例
                            .addLast(serverHandler);
                    }
                });
            //  异步地绑定服务器；调用sync()方法阻塞等待直到绑定完成
            ChannelFuture channelFuture = serverBootstrap.bind()
                .sync();
            // 获取Channel的CloseFuture，并且阻塞当前线程直到它完成
            channelFuture.channel()
                .closeFuture()
                .sync();
        } finally {
            // 关闭EventLoopGroup，释放所有的资源
            eventLoopGroup.shutdownGracefully()
                .sync();
        }
    }
}
