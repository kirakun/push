package com.netease.push.socket;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharacterCodingException;
import java.util.concurrent.Future;

import com.netease.push.message.CharsetHelper;
import com.netease.push.message.MessageListener;

/**
 * 封装socket
 * @author zhengxiaobin
 *
 */
public class Socket {

	private AsynchronousSocketChannel channel;

	private State state = State.DEFAULT;

	private String user;// 标记用户账户
	private String device;// 标记用户设备

	public Socket(AsynchronousSocketChannel channel) {
		this.channel = channel;
		this.state = State.CONNECTED;
	}

	public void setState(State state) {
		this.state = state;
	}

	public AsynchronousSocketChannel getChannel() {
		return this.channel;
	}

	public void setAccount(String user, String device) {
		this.user = user;
		this.device = device;
	}

	// 同步方法
	public Future<Integer> sendMessage(String msg) throws CharacterCodingException {
		return channel.write(CharsetHelper.encode(CharBuffer.wrap(msg)));
	}

	// 异步方法
	public void sendMessageAsyn(String msg, final MessageListener mListener) throws CharacterCodingException {
		final ByteBuffer byteBuffer = CharsetHelper.encode(CharBuffer.wrap(msg));
		channel.write(byteBuffer, null, new CompletionHandler<Integer, Object>() {
			@Override
			public void completed(Integer result, Object attachment) {
				if (byteBuffer.hasRemaining()) {
					channel.write(byteBuffer, null, this);
				} else {
					if (mListener != null)
						mListener.onComplete(byteBuffer);
				}
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				if (mListener != null)
					mListener.onFail(exc);
			}
		});
	}

}
