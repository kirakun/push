package com.netease.push.handler;

import com.netease.push.context.ApplicationContext;
import com.netease.push.socket.Socket;

/**
 * 拦截器  对客户端的请求数据做过滤处理
 * 使用责任链模式
 * @author zhengxiaobin
 *
 */
public interface MessageHandler {

	/**
	 *  前置处理
	 * @param context
	 * @param socket
	 * @param byteBuffer
	 * @return true代表处理中断
	 */
	boolean preHandle(ApplicationContext context, Socket socket, String response);

	/**
	 * 后置处理
	 * @param context
	 * @param socket
	 * @param response
	 */
	void afterComplete(ApplicationContext context, Socket socket, String response);
}
