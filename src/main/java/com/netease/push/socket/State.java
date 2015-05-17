package com.netease.push.socket;

/**
 * 长连接状态
 * @author zhengxiaobin
 *
 */
public enum State {

	DEFAULT(-1),CONNECTED(0), ACTIVE(1), CLOSED(2);
	private int value;

	private State(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}
}
