package com.netease.push.context;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharacterCodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import com.netease.push.handler.MessageHandler;
import com.netease.push.handler.MessageHandlerChain;
import com.netease.push.message.CharsetHelper;
import com.netease.push.socket.Socket;

/**
 * 上下文环境
 * 
 * @author zhengxiaobin
 *
 */
public class ApplicationContext {

	private AsynchronousServerSocketChannel server;

	// key=hashcode value=socket
	protected Map<Integer, Socket> clients;

	private int port;

	protected MessageHandlerChain mMessageHandlerChain;

	public ApplicationContext(int port) {
		this.port = port;
		this.clients = new HashMap<Integer, Socket>();
		try {
			init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void init() throws IOException {
		AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup.withFixedThreadPool(Runtime.getRuntime()
				.availableProcessors(), Executors.defaultThreadFactory());
		server = AsynchronousServerSocketChannel.open(channelGroup);
		// 重用端口
		server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		// 绑定端口并设置连接请求队列长度
		server.bind(new InetSocketAddress(port), 0);
		server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {

			@Override
			public void completed(final AsynchronousSocketChannel channel, Object attachment) {
				// TODO Auto-generated method stub
				onConnected(channel);
				// 先安排处理下一个连接请求，异步非阻塞调用，所以不用担心挂住了
				// 这里传入this是个地雷，小心多线程
				server.accept(null, this);
				// 处理连接读写
				final ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);
				readBuffer.clear();
				channel.read(readBuffer, null, new CompletionHandler<Integer, Object>() {
					@Override
					public void completed(Integer count, Object attachment) {
						if (count > 0) {
							readBuffer.flip();
							try {
								String msg = CharsetHelper.decode(readBuffer).toString();
								if (mMessageHandlerChain != null) {
									Socket socket = clients.get(channel.hashCode());
									if (!mMessageHandlerChain.doFilter(ApplicationContext.this, socket, msg)) {
										onMessageReceive(channel, msg);
										mMessageHandlerChain.afterFilter(ApplicationContext.this, socket, msg);
									}
								} else {
									onMessageReceive(channel, msg);
								}

							} catch (CharacterCodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								try {
									onDisconnect(channel);
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}

						} else {
							try {
								// 如果客户端关闭socket，那么服务器也需要关闭，否则浪费CPU
								onDisconnect(channel);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						// 异步调用OS处理下个读取请求
						// 这里传入this是个地雷，小心多线程
						channel.read(readBuffer, null, this);
					}

					@Override
					public void failed(Throwable exc, Object attachment) {
						System.out.println("server read failed: " + exc);
						if (channel != null) {
							try {
								onDisconnect(channel);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				});
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				// TODO Auto-generated method stub
				System.out.println("client connect fail");
			}

		});

	}
	/**
	 * 添加处理链
	 * @param handlers
	 */
	public void addHandlers(List<MessageHandler> handlers) {
		mMessageHandlerChain = new MessageHandlerChain(handlers);
	}

	protected void onMessageReceive(AsynchronousSocketChannel channel, String message) {
		System.out.println("onMessageReceive:" + message);
	}

	protected void onConnected(AsynchronousSocketChannel channel) {
		clients.put(channel.hashCode(), new Socket(channel));
	}

	protected void onDisconnect(AsynchronousSocketChannel channel) throws IOException {
		channel.close();
		clients.remove(channel.hashCode());
	}

}
