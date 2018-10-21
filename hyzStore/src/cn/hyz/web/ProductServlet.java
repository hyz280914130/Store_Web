package cn.hyz.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;

import com.google.gson.Gson;

import cn.hyz.domain.Cart;
import cn.hyz.domain.CartItem;
import cn.hyz.domain.Category;
import cn.hyz.domain.Order;
import cn.hyz.domain.OrderItem;
import cn.hyz.domain.PageBean;
import cn.hyz.domain.Product;
import cn.hyz.domain.User;
import cn.hyz.service.ProductService;
import cn.hyz.utils.CommonsUtils;
import cn.hyz.utils.JedisPoolUtils;
import cn.hyz.utils.PaymentUtil;
import redis.clients.jedis.Jedis;

public class ProductServlet extends Base {
	
	
	
	//5.这个是首页显示最新最热的方法
	public void index(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		ProductService service = new ProductService();
		//查询热门商品
		List<Product> hotProduct = service.findHotProduct();
		
		//查询最新商品
		List<Product> newProduct = service.findNewProduct();
		
		//转发
		request.setAttribute("hotProduct", hotProduct);
		request.setAttribute("newProduct", newProduct);
		
		request.getRequestDispatcher("/index.jsp").forward(request, response);
	}
	
	//6.这个是首页herder的分类方法
	public void category(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		ProductService service = new ProductService();
		
		//获取jedis对象，连接jedis
		Jedis jedis = JedisPoolUtils.getJedis();
		//获取jedis中的数据
		String categoryJson = jedis.get("categoryJson");
		//判断jedis中数据是否为空
		if(categoryJson==null) {
			//如果为空则从数据库中获取
			System.out.println("redis是空的");
			List<Category> category = service.findCategory();
			Gson gson = new Gson();
			categoryJson = gson.toJson(category);
			jedis.set("categoryJson", categoryJson);
		}
		
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().write(categoryJson);
		
	}
	
	//7.这个是product_list页面的相关方法，包括分页显示，历史纪录
	public void productByCid(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int currentCount = 12;
		//获取数据
		String cid = request.getParameter("Cid");
		String currentPageStr = request.getParameter("currentPage");
		if(currentPageStr==null) currentPageStr="1";
		int currentPage = Integer.parseInt(currentPageStr);
		//System.out.println(currentPageStr);
		//根据cid查询数据
		ProductService service = new ProductService();
		PageBean pageBean = null;
		try {
			pageBean = service.ProductListByCid(cid,currentPage,currentCount);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		request.setAttribute("pageBean", pageBean);
		request.setAttribute("cid", cid);
		//System.out.println("产品列表"+pageBean.getList());
		
		//定义一个记录历史商品信息的集合
		List<Product> historyProductList = new ArrayList<Product>();
		
		//获得客户端携带名字叫pids的cookie
		Cookie[] cookies = request.getCookies();
		if(cookies!=null){
			for(Cookie cookie:cookies){
				if("pids".equals(cookie.getName())){
					String pids = cookie.getValue();//3-2-1
					String[] split = pids.split("-");
					for(String pid : split){
						Product pro = null;
						try {
							pro = service.getProductByPid(pid);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						historyProductList.add(pro);
					}
				}
			}
		}
		
		//将历史记录的集合放到域中
		request.setAttribute("historyProductList", historyProductList);
		
		request.getRequestDispatcher("/product_list.jsp").forward(request, response);
	}
	
	//8.这个是商品详细信息页的显示
	public void productInfo(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pid = request.getParameter("pid");
		String cid = request.getParameter("cid");
		String currentPage = request.getParameter("currentPage");
		//System.out.println("currentPage:"+currentPage);
		
		//这而不需要在转一次了，我的数据存在这儿是要传给ProductByCid的，但是那边已经有转换的功能了，这边还转就出错。
		//int currentPage = Integer.parseInt(currentPageStr);
		
		ProductService service = new ProductService();
		Product product = null;
		try {
			product = service.getProductByPid(pid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		request.setAttribute("product", product);
		request.setAttribute("cid", cid);
		request.setAttribute("currentPage", currentPage);
		
		//获得客户端携带的cookie --- 获得名字是pids的cookie
		String pids = pid;
		Cookie[] cookies = request.getCookies();
		if(cookies!=null){
			for(Cookie cookie : cookies){
				if("pids".equals(cookie.getName())){
					pids = cookie.getValue();
					//1-3-2 本次访问商品pid是8----->8-1-3-2
					//1-3-2 本次访问商品pid是3----->3-1-2
					//1-3-2 本次访问商品pid是2----->2-1-3
					//将pids拆成一个数组
					String[] split = pids.split("-");//{3,1,2}
					List<String> asList = Arrays.asList(split);//[3,1,2]
					LinkedList<String> list = new LinkedList<String>(asList);//[3,1,2]
					//判断集合中是否存在当前pid
					if(list.contains(pid)){
						//包含当前查看商品的pid
						list.remove(pid);
						list.addFirst(pid);
					}else{
						//不包含当前查看商品的pid 直接将该pid放到头上
						list.addFirst(pid);
					}
					//将[3,1,2]转成3-1-2字符串
					StringBuffer sb = new StringBuffer();
					for(int i=0;i<list.size()&&i<7;i++){
						sb.append(list.get(i));
						sb.append("-");//3-1-2-
					}
					//去掉3-1-2-后的-
					pids = sb.substring(0, sb.length()-1);
				}
			}
		}
		
		Cookie cookie_pids = new Cookie("pids",pids);
		response.addCookie(cookie_pids);
		
		request.getRequestDispatcher("/product_info.jsp").forward(request, response);
	}
	
	//8.这个方法是将商品添加到购物车
		public void addProductToCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {

			HttpSession session = request.getSession();
			ProductService service = new ProductService();

			//获得要放到购物车的商品的pid
			String pid = request.getParameter("pid");
			//获得该商品的购买数量
			int buyNum = Integer.parseInt(request.getParameter("buyNum"));
			//获得product对象
			Product product = service.getProductByPid(pid);
			//计算小计
			double subtotal = product.getShop_price()*buyNum;
			//封装CartItem
			CartItem item = new CartItem();
			item.setProduct(product);
			item.setBuyNum(buyNum);
			item.setSubtotal(subtotal);
			//获得购物车---判断是否在session中已经存在购物车
			Cart cart = (Cart) session.getAttribute("cart");
			if(cart==null){
				cart = new Cart();
			}
			//将购物项放到车中---key是pid
			//先判断购物车中是否已将包含此购物项了 ----- 判断key是否已经存在
			//如果购物车中已经存在该商品----将现在买的数量与原有的数量进行相加操作
			Map<String, CartItem> cartItems = cart.getCartItems();
			
			double newsubtotal = 0.0;
			if(cartItems.containsKey(pid)){
				//取出原有商品的数量
				CartItem cartItem = cartItems.get(pid);
				int oldBuyNum = cartItem.getBuyNum();
				oldBuyNum+=buyNum;
				cartItem.setBuyNum(oldBuyNum);
				cart.setCartItems(cartItems);
				//修改小计
				//原来该商品的小计
				double oldsubtotal = cartItem.getSubtotal();
				//新买的商品的小计
				newsubtotal = buyNum*product.getShop_price();
				cartItem.setSubtotal(oldsubtotal+newsubtotal);
			}else{
				//如果车中没有该商品
				cart.getCartItems().put(product.getPid(), item);
				newsubtotal = buyNum*product.getShop_price();
			}
			//计算总计
			double total = cart.getTotal()+newsubtotal;
			cart.setTotal(total);

			//将车再次访问session
			session.setAttribute("cart", cart);

			//直接跳转到购物车页面
			response.sendRedirect(request.getContextPath()+"/cart.jsp");
		}
	
//	public void addProductToCart(HttpServletRequest request, HttpServletResponse response)
//			throws ServletException, IOException, SQLException {
//		
//		//System.out.println("进来没");
//		
//		//获得要放到购物车的商品的pid
//		String pid = request.getParameter("pid");
//		//获得该商品的购物数量
//		String buyNumStr = request.getParameter("buyNum");
//		int buyNum = Integer.parseInt(buyNumStr);
//		//获得product对象
//		ProductService service = new ProductService();
//		Product product = service.getProductByPid(pid);
//		//计算小计
//		double subtotal = product.getShop_price()*buyNum;
//		//封装CartItem
//		CartItem item = new CartItem();
//		item.setBuyNum(buyNum);
//		item.setProduct(product);
//		item.setSubtotal(subtotal);
//		//获得购物车 -- 判断是否在session中已经存在购物车，如果没有则创建
//		HttpSession session = request.getSession();
//		Cart cart = (Cart) session.getAttribute("cart");
//		if(cart==null) {
//			cart = new Cart();
//		}
//		//将订单项放入购物车车中,但是要先进行判断，看看是否之前已经有过相同的订单了
//			//先获取订单项的集合，一会才可以放入
//		Map<String, CartItem> cartItems = cart.getCartItems();
//		if(cartItems.containsKey(pid)) {
//			//如果存在，则把价格和数量累加
//			
//			//取出原来订单项中的小计、数量
//			CartItem oldItem = cartItems.get(pid);
//			double OldSubtotal = oldItem.getSubtotal();
//			int OldBuyNum = oldItem.getBuyNum();
//			//重新计算小计和数量
//			subtotal+=OldSubtotal;
//			buyNum+=OldBuyNum;
//			//重新封装订单项
//			item.setBuyNum(buyNum);
//			item.setSubtotal(subtotal);
//			//重新放入订单项集合
//			cartItems.put("pid", item);
//			//设置cart里订单项集合，取出来放入内容后还要放回去
//			cart.setCartItems(cartItems);
//		}else {
//			//如果没有，则就直接封装cartItems放入cart
//			cartItems.put("pid", item);
//			cart.setCartItems(cartItems);
//		}
//		//计算总计
//		double newsubtotal = item.getSubtotal();
//		double total = cart.getTotal()+newsubtotal;
//		cart.setTotal(total);
//		//放入session中
//		session.setAttribute("cart", cartItems);
//		//跳转到购物车页面
//		response.sendRedirect(request.getContextPath()+"/cart.jsp");
//	}
		
		//9.删除订单项
		public void delProFromCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			//获得要删除的item的pid
			String pid = request.getParameter("pid");
			//删除session中的购物车中的购物项集合中的item
			HttpSession session = request.getSession();
			Cart cart = (Cart) session.getAttribute("cart");
			if(cart!=null){
				Map<String, CartItem> cartItems = cart.getCartItems();
				//需要修改总价
				cart.setTotal(cart.getTotal()-cartItems.get(pid).getSubtotal());
				//删除
				cartItems.remove(pid);
				cart.setCartItems(cartItems);

			}

			session.setAttribute("cart", cart);

			//跳转回cart.jsp
			response.sendRedirect(request.getContextPath()+"/cart.jsp");
		}
		
		//10.清空购物车
		public void clearCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			HttpSession session = request.getSession();
			session.removeAttribute("cart");
			//跳转回cart.jsp
			response.sendRedirect(request.getContextPath()+"/cart.jsp");

		}
		
		//11.提交订单submitOrder
		public void submitOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ParseException {
			
			HttpSession session = request.getSession();
			//判断用户是否已经登陆，如果未登录下面的代码不执行
			User user = (User) session.getAttribute("user");
			if(user==null) {
				response.sendRedirect(request.getContextPath()+"/login.jsp");
				//虽然转发但是下面的代码还是会执行，我们要return，这样就不会执行下面这个循环
				return;
			}
			//目的：封装好一个Order对象，传递给service层
			Order order = new Order();
			//1.封装订单的订单号
			String uuid = CommonsUtils.getUUID();
			order.setOid(uuid);
			//2.封装下单时间
			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			String date1 = format.format(date);
			order.setOrdertime(format.parse(date1));
			//3.封装订单的总金额
			Cart cart = (Cart) session.getAttribute("cart");
			double total = cart.getTotal();
			order.setTotal(total);
			//4.封装订单状态，0代表为付款，1代表已付款
			order.setState(0);
			//5.封装收获地址
			order.setAddress(null);
			//6.封装收货人
			order.setName(null);
			//7.封装收货人电话
			order.setTelephone(null);
			//8.封装订单所属的客户
			order.setUser(user);
			//9.封装订单中的订单项
			Map<String, CartItem> cartItems = cart.getCartItems();
			//需要用一个for循环封装里面的每一项，cartItems.entrySet()获得对象
			for(Map.Entry<String, CartItem> entry : cartItems.entrySet()) {
				//取出每一个购物项
				CartItem cartItem = entry.getValue();
				//获得订单项的对象，注意和上面的不一样，虽然差不多，但是不一样，避免对象污染
				OrderItem orderItem = new OrderItem();
				//1.封装订单项的id
				orderItem.setItemid(CommonsUtils.getUUID());
				//2.封装订单项内商品的购物数量
				orderItem.setCount(cartItem.getBuyNum());
				//3.封装封装订单项小计
				orderItem.setSubtotal(cartItem.getSubtotal());
				//4.封装订单项内部的商品
				orderItem.setProduct(cartItem.getProduct());
				//5.该订单项术属于哪一个订单
				orderItem.setOrder(order);
				//将该订单添加到订单的订单项集合中
				order.getOrderItems().add(orderItem);
			}
			//order对象已经封装完成，传递到service层
			ProductService service = new ProductService();
			service.submitOrder(order);
			
			session.setAttribute("order", order);
			//System.out.println("执行了。。。。");
			//页面跳转
			response.sendRedirect(request.getContextPath()+"/order_info.jsp");
			
			
		}
		
		//12.确认订单，更新收货人信息，还有完成支付
		public void confirmOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			
			//1.更新收货人信息
			Map<String, String[]> properties = request.getParameterMap();
			Order order = new Order();
			try {
				BeanUtils.populate(order, properties);
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
			ProductService service = new ProductService();
			service.updateOrderAdrr(order);
			//2.支付
			String orderid = request.getParameter("oid");
			//String money = order.getTotal()+"";//支付金额
			String money = "0.01";//支付金额
			// 银行
			String pd_FrpId = request.getParameter("pd_FrpId");
			// 发给支付公司需要哪些数据
			String p0_Cmd = "Buy";
			String p1_MerId = ResourceBundle.getBundle("merchantInfo").getString("p1_MerId");
			String p2_Order = orderid;
			String p3_Amt = money;
			String p4_Cur = "CNY";
			String p5_Pid = "";
			String p6_Pcat = "";
			String p7_Pdesc = "";
			// 支付成功回调地址 ---- 第三方支付公司会访问、用户访问
			// 第三方支付可以访问网址
			String p8_Url = ResourceBundle.getBundle("merchantInfo").getString("callback");
			String p9_SAF = "";
			String pa_MP = "";
			String pr_NeedResponse = "1";
			// 加密hmac 需要密钥
			String keyValue = ResourceBundle.getBundle("merchantInfo").getString(
					"keyValue");
			String hmac = PaymentUtil.buildHmac(p0_Cmd, p1_MerId, p2_Order, p3_Amt,
					p4_Cur, p5_Pid, p6_Pcat, p7_Pdesc, p8_Url, p9_SAF, pa_MP,
					pd_FrpId, pr_NeedResponse, keyValue);
			String url = "https://www.yeepay.com/app-merchant-proxy/node?pd_FrpId="+pd_FrpId+
					"&p0_Cmd="+p0_Cmd+
					"&p1_MerId="+p1_MerId+
					"&p2_Order="+p2_Order+
					"&p3_Amt="+p3_Amt+
					"&p4_Cur="+p4_Cur+
					"&p5_Pid="+p5_Pid+
					"&p6_Pcat="+p6_Pcat+
					"&p7_Pdesc="+p7_Pdesc+
					"&p8_Url="+p8_Url+
					"&p9_SAF="+p9_SAF+
					"&pa_MP="+pa_MP+
					"&pr_NeedResponse="+pr_NeedResponse+
					"&hmac="+hmac;

			//重定向到第三方支付平台
			response.sendRedirect(url);
		}
		
		//13.获得我的订单
		public void myOrders(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			
			//有session代表用户已经登陆了
			HttpSession session = request.getSession();
			User user = (User) session.getAttribute("user");
			//如果用户没登陆
			if(user==null) {
				response.sendRedirect(request.getContextPath()+"/login.jsp");
				//虽然转发但是下面的代码还是会执行，我们要return，这样就不会执行下面这个循环
				return;
			}
			//查询该用户的所有订单信息,但是缺少List<OrderItem> orderItems
			ProductService service = new ProductService();
			List<Order> orderList = service.findAllOrders(user.getUid());
			//循环遍历所有的订单，为每个订单填充订单集合信息
			if(orderList!=null) {
				for(Order order : orderList) {
					//获得每一个订单的oid
					String oid = order.getOid();
					//查询订单的所有订单项，订单项是内容是orderItem和product加起来的数据，
					//所有用一般的BeanListHandler不能指定类型，是不能封装的，我们用MapList先获取，
					//然后下面在重新封装
					List<Map<String,Object>> mapList = service.findAllOrderItemByOid(oid);
					//将mapList中的数据取出来分别封装
					for(Map<String,Object> map : mapList) {
						try {
							//从map中取出数据封装到OrderItem
							OrderItem orderItem = new OrderItem();
							BeanUtils.populate(orderItem, map);
							//从mao中取出数据封装到Product中
							Product product = new Product();
							BeanUtils.populate(product, map);
							//将product封装到OrderItemList中
							orderItem.setProduct(product);
							//将orderItem封装到Order中
							//order.setOrderItems(orderItem); 这个是错误的写法
							order.getOrderItems().add(orderItem);
						} catch (IllegalAccessException | InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			//orderList封装完整了，现在转发给页面
			request.setAttribute("orderList", orderList);
			request.getRequestDispatcher("/order_list.jsp").forward(request, response);
		}
	
	
	


}