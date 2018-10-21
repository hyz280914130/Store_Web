package cn.hyz.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import cn.hyz.domain.Category;
import cn.hyz.domain.Order;
import cn.hyz.service.AdminService;

public class AdminServlet extends Base {
	
	//查询所有分类
	public void findAllCategory(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		/*
		 * 查询分类并转为json
		 */
		AdminService service = new AdminService();
		List<Category> category = service.findAllCategory();
		
		Gson gson = new Gson();
		String json = gson.toJson(category);
		//System.out.println(json);
		
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(json);
	}
	
	//查询所有订单
	public void findAllOrders(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		//获取订单的信息
		AdminService service = new AdminService();
		List<Order> orderList = service.findAllOrders();
		//存到域中
		request.setAttribute("orderList", orderList);
		//设置编码
		response.setCharacterEncoding("UTF-8");
		//转发给页面
		request.getRequestDispatcher("admin/order/list.jsp").forward(request, response);
		
	}
	
	//查询订单详情
	public void findOrderInfoByOid(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		//模拟加载数据延迟
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//获取json的参数
		String oid = request.getParameter("oid");
		//查询订单详情的数据
		AdminService service = new AdminService();
		List<Map<String,Object>> map = service.findOrderInfoByOid(oid);
		//转为json格式
		Gson gson = new Gson();
		String json = gson.toJson(map);
		//System.out.println(json);
		//设置编码
		response.setContentType("text/html;charset=UTF-8");
		//传给前台
		response.getWriter().write(json);
	}
	

}