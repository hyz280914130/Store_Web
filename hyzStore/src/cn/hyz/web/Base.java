package cn.hyz.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Base extends HttpServlet {
	
	@Override
	//不写doGet和doPost方法的原因是，这两个方法最初都是要经过service调用
	/*
	 *	当服务器收到请求，会到web.xml中查找相应的servlet，如果是第一次访问会将这个servlet加载到内存并实例化，也就是servlet容器启动，
	 *	servlet容器第一次启动时会创建contextConfig对象，contextConfig对象会调用init()方法，
	 *	然后在Servlet API中有一个ServletContextListener接口，它能监听ServletContext对象的生命周期，
	 *	在Servlet容器启动时，他会把contextConfig放入ServletContext容器中。
	 *	之后servlet容器收到请求，就会调用servlet中的service方法进行转向，但是此时已经不需要doGet和doPost方法了，
	 *	直接通过this来获取字节码对象，这个this是Base的子类实例，因为在他那儿找不到service方法，所以跑到父类中来找，所以当前执行service方法的是子类，
	 *	故而this代表子类实例
	 */
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		//req.setCharacterEncoding("UTF_8");
		
		try {
			//获得请求的method方法
			String methodName = req.getParameter("method");
			//获得当前被访问的对象的字节码对象
			Class clazz = this.getClass();
			//获得当前字节码对象中的指定方法
			//参数传的是类型的字节码
			Method method = clazz.getMethod(methodName, HttpServletRequest.class,HttpServletResponse.class);
			//执行相应的功能方法
			method.invoke(this, req,resp);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

}