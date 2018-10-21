package cn.hyz.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;

import cn.hyz.domain.User;
import cn.hyz.service.UserService;
import cn.hyz.utils.CommonsUtils;
import cn.hyz.utils.MailUtils;

public class UserServlet extends Base {
	
	//1.注册方法
	public void register(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//设置编码
		request.setCharacterEncoding("UTF-8");
		//接收参数
		Map<String, String[]> properties = request.getParameterMap();
		//修改date
		/*
		 * 当用到BeanUtils的populate、copyProperties方法或者getProperty,setProperty方法其实都会调用convert进行转换，
		 * 只支持一些基本类型转换，不支持Data类型转换。，所以当输入的日期是字符串，不会自动和javabean里的属性匹配，
		 * 需要吧字符串转为日期类型，使用ConverUtils
		 */
		//ConvertUtils.register(new DateLocaleConverter(), clazz);
		ConvertUtils.register(new Converter() {
			@Override
			//value是前端接收的字符串数据，转换器转换完之后返回转化好的数据
			public Object convert(Class clazz, Object value) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Date date = null;
				try {
					//这儿用的是parse，将字符串转为日期
					date = format.parse(value.toString());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				return date;
			}
		}, Date.class);
		
		//封装参数
		User user = new User();
		try {
			BeanUtils.populate(user, properties);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		//封装其他参数
		//private String uid;
		user.setUid(CommonsUtils.getUUID());
		//private String telephone;
		user.setTelephone(null);
		//private int state;//是否激活
		user.setState(0);
		String activeCode = CommonsUtils.getUUID();
		user.setCode(activeCode);
		
		//传递给service层处理
		UserService service = new UserService();
		boolean isRegisteSuccess = service.regist(user);
		
		if(isRegisteSuccess) {
			//注册成功发送注册邮件
			//把所有用户相关的方法移到UserServlet里面来以后下面的url还没有更改。
			String emailMsg = "恭喜您注册成功，请点击下面的连接进行激活账户"
					+ "<a href='http://localhost:8080/hyzStore/active?activeCode="+activeCode+"'>"
							+ "http://localhost:8080/hyzStore/active?activeCode="+activeCode+"</a>";
			try {
				MailUtils.sendMail(user.getEmail(), emailMsg);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
			//跳转到登陆界面
			response.sendRedirect(request.getContextPath()+"/login.jsp");
		}
		
	}
	
	//2.这个是用户激活的方法
	public void active(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String activeCode = request.getParameter("activeCode");
		
		//这个是ActiveServlet_02
		
		//传递给service
		UserService service = new UserService();
		try {
			service.active(activeCode);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//跳转到登陆页面
		response.sendRedirect(request.getContextPath()+"/login.jsp");
		
	}
	
	//3.这个是ajax异步检查用户名是否重复
	public void checkUsername(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String username = request.getParameter("username");

		UserService service = new UserService();
		boolean isExist = false;
		try {
			isExist = service.checkUsername(username);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String json = "{\"isExist\":"+isExist+"}";
		response.getWriter().write(json);
	}
	
	//4.这个是登陆方法
	public void login(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		//将用户名和密码传给service层操作
		UserService service = new UserService();
		User user = service.login(username,password);
		
		//判断用户是否为空
		if(user!=null) {
			//判断用户是否勾选了自动登陆
			String autoLogin = request.getParameter("autoLogin");
			if("true".equals(autoLogin)) {
				//把用户名存到cookie中
				Cookie cookie_username = new Cookie("cookie_user",user.getUsername());
				cookie_username.setMaxAge(10*60);
				//创建存储密码的cookie
				Cookie cookie_password = new Cookie("cookie_password",user.getPassword());
				cookie_username.setMaxAge(10*60);
				
				response.addCookie(cookie_username);
				response.addCookie(cookie_password);
			}
			//将user对象存到session中
			HttpSession session = request.getSession();
			session.setAttribute("user", user);
			//重定向到首页
			response.sendRedirect(request.getContextPath()+"/index.jsp");
		}else{
			request.setAttribute("loginError", "用户名或密码错误");
			request.getRequestDispatcher("/login.jsp").forward(request, response);
		}
		
	}
	
	//5.这个是注销方法
	public void logout(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		//删除session中的user
		HttpSession session = request.getSession();
		session.removeAttribute("user");
		//跳转到登陆页面
		response.sendRedirect(request.getContextPath()+"/login.jsp");
		//删除cookie
		Cookie cookie_username = new Cookie("cookie_user","");
		cookie_username.setMaxAge(0);
		Cookie cookie_password = new Cookie("cookie_password","");
		cookie_username.setMaxAge(0);
		response.addCookie(cookie_username);
		response.addCookie(cookie_password);
		//跳转到login
		response.sendRedirect(request.getContextPath()+"/login.jsp");
	}


}