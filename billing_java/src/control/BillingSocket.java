/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import java.net.InetSocketAddress;
import main.Billing;
import model.Configuration;
import model.Packet;

/**
 *
 * @author hopel
 */
public class BillingSocket {

    private ServerBootstrap server;
    private Configuration configuration;

    public BillingSocket() {
        this.configuration = Billing.getInstance().getConfiguration();
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            this.server = new ServerBootstrap();
            this.server.group(group);
            this.server.channel(NioServerSocketChannel.class);
            this.server.localAddress(new InetSocketAddress(this.configuration.getBillingIp(), this.configuration.getBillingPort()));
            this.server.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new BillingSocketHandler());
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void start() {
        try {
            ChannelFuture channelFuture = this.server.bind().sync();
            System.out.println("Billing started completely within ServerId = " + this.configuration.getServerId() + ", waiting for server...");
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public class BillingSocketHandler extends ChannelInboundHandlerAdapter {

        private Packet solvePacket(byte incommingPacket[]) {
            final Packet packet = new Packet();

            if (ResolvePacket.isOpeningSocketPacket(incommingPacket)) {
                packet.setPacket(ResolvePacket.solveOpeningSocketPacket(incommingPacket));
                packet.setType("Open connection");
            } else if (ResolvePacket.isHandlingPacket(incommingPacket)) {
                packet.setPacket(ResolvePacket.solveHandlingPacket(incommingPacket));
                packet.setType("Synchronize connection");
            } else if (ResolvePacket.isLoginPacket(incommingPacket)) {
                packet.setPacket(ResolvePacket.solveLoginPacket(incommingPacket));
                packet.setType("Account login");
            } else if (ResolvePacket.isSelectCharacterPacket(incommingPacket)) {
                packet.setPacket(ResolvePacket.solveSelectCharacterPacket(incommingPacket));
                packet.setType("Character selection");
            } else if (ResolvePacket.isOutGamePacket(incommingPacket)) {
                packet.setPacket(ResolvePacket.solveOutGamePacket(incommingPacket));
                packet.setType("Character logout");
            } else if (ResolvePacket.isAskPointPacket(incommingPacket)) {
                packet.setPacket(ResolvePacket.solveAskPointPacket(incommingPacket));
                packet.setType("Check point");
            } else if (ResolvePacket.isExchangePointPacket(incommingPacket)) {
                packet.setPacket(ResolvePacket.solveExchangePointPacket(incommingPacket));
                packet.setType("Exchange point");
            } else if (ResolvePacket.isActivateCodePacket(incommingPacket)) {
                packet.setPacket(ResolvePacket.solveActivateCodePacket(incommingPacket));
                packet.setType("Activate code");
            } else if (ResolvePacket.isCheckCharacterOnlinePacket(incommingPacket)) {
                packet.setPacket(ResolvePacket.solveCheckCharacterOnlinePacket(incommingPacket));
                packet.setType("Online character synchronize");
            } else if (ResolvePacket.isGetCodeRewardPacket(incommingPacket)) {
                packet.setPacket(ResolvePacket.solveGetCodeRewardPacket(incommingPacket));
                packet.setType("Check giftcode awards");
            }

            return packet;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf inBuffer = (ByteBuf) msg;

            // TODO use instead inBuffer.METHOD
            byte[] b = ByteBufUtil.getBytes(inBuffer);
            Packet packet = this.solvePacket(b);
            if (packet.getType() != null && packet.getType().equals("Open connection")) {
                System.out.println("Success connect to server " + BillingSocket.this.configuration.getBillingIp() + ":" + BillingSocket.this.configuration.getBillingPort());
            }

            if (packet.getPacket() != null) {
                ctx.writeAndFlush(Unpooled.copiedBuffer(packet.getPacket()));
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            // ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }

    }

}
