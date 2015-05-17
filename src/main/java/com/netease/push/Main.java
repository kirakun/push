package com.netease.push;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import com.netease.push.context.ApplicationContext;

/**
 * 
 * @author zhengxiaobin 程序入口
 */
public class Main {
	public static void main(String[] args) throws IOException {
		int port = 8000;
		if (args != null && args.length > 0) {
			if (StringUtils.isNumeric(args[0])) {
				port = Integer.parseInt(args[0]);
			} else {
				System.out.println("the first param must be number");
				System.exit(1);
			}
		} else {
			System.out.println("set the default port is 8000");
		}

		new ApplicationContext(port);

	}
}
