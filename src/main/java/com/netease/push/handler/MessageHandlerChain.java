package com.netease.push.handler;

import java.util.List;

import com.netease.push.context.ApplicationContext;
import com.netease.push.socket.Socket;

public class MessageHandlerChain {

	private List<MessageHandler> mMessageHandler;

	public MessageHandlerChain(List<MessageHandler> handlers) {
		this.mMessageHandler = handlers;
	}

	public boolean doFilter(ApplicationContext context, Socket socket, String response) {
		if (mMessageHandler == null || mMessageHandler.size() == 0) {
			return false;
		} else {
			for (MessageHandler handler : mMessageHandler) {
				if (handler.preHandle(context, socket, response)) {
					return true;
				}
			}
		}
		return false;
	}

	public void afterFilter(ApplicationContext context, Socket socket, String response) {
		if (mMessageHandler == null || mMessageHandler.size() == 0) {
			return;
		} else {
			for (MessageHandler handler : mMessageHandler) {
				handler.afterComplete(context, socket, response);
			}
		}
	}

}
