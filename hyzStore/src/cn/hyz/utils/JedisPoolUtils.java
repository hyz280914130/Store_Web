package cn.hyz.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolUtils {
	
	private static JedisPool pool = null;
	
	static {
		//加载配置文件
			//相对地址 src
		InputStream in = JedisPoolUtils.class.getClassLoader().getResourceAsStream("redis.properties");
		//用这个对象读取配置文件
		Properties pro = new Properties();
		try {
			pro.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//获得池子对象
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxIdle(Integer.parseInt(pro.get("redis.maxIdle").toString()));//最大闲置个数
		poolConfig.setMinIdle(Integer.parseInt(pro.get("redis.minIdle").toString()));//最小闲置个数
		poolConfig.setMaxTotal(Integer.parseInt(pro.get("redis.maxTotal").toString()));//最大连接数
		//第一个参数是相关配置，第二个参数是地址，第三个参数是端口
		pool = new JedisPool(poolConfig,pro.getProperty("redis.url"),Integer.parseInt(pro.getProperty("redis.port").toString()));
	}
	
	//获得Jedsi资源的方法
	public static Jedis getJedis() {
		Jedis resource = pool.getResource();
		return resource;
	}
	
	//这个其实是测试，没什么用途
	public static void main(String[] args) {
		Jedis jedis = getJedis();
		System.out.println(jedis.get("xxx"));
	}

	public void close() {
		pool.close();
		
	}
	


}
