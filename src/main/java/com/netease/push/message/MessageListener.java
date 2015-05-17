package com.netease.push.message;

import java.nio.ByteBuffer;

/**
 * 发送消息监听器
 * @author zhengxiaobin
 *
 */
public interface MessageListener {

	void onComplete(ByteBuffer byteBuffer);

	void onFail(Throwable e);

}
