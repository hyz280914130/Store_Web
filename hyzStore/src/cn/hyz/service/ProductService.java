package cn.hyz.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import cn.hyz.dao.ProductDao;
import cn.hyz.domain.Product;
import cn.hyz.utils.DataSourceUtils;
import cn.hyz.domain.Category;
import cn.hyz.domain.Order;
import cn.hyz.domain.PageBean;

public class ProductService {
	
	public List<Product> findHotProduct() {
		ProductDao dao = new ProductDao();
		List<Product> hotProduct = null;
		try {
			hotProduct = dao.findHotProduct();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hotProduct;
	}


	public List<Product> findNewProduct() {
		ProductDao dao = new ProductDao();
		List<Product> newProduct = null;
		try {
			newProduct = dao.findNewProduct();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		return newProduct;
	}


	public List<Category> findCategory() {
		ProductDao dao = new ProductDao();
		List<Category> category = null;
		try {
			category = dao.findCategory();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		return category;
	}


	public PageBean ProductListByCid(String cid, int currentPage, int currentCount) throws SQLException {
		ProductDao dao = new ProductDao();
		
		//System.out.println("cid:"+cid+" currentPage:"+currentPage+" currentCount:"+currentCount);
		//要添加泛型
		PageBean<Product> pageBean = new PageBean<Product>();
		
		//1.封装当前页
		pageBean.setCurrentPage(currentPage);
		//2.封装每页显示的条数
		pageBean.setCurrentCount(currentCount);
		//3.封装总条数
		int totalCount = dao.getTotalCount(cid);
		pageBean.setTotalCount(totalCount);
		//System.out.println("总页数为"+totalCount);
		//4.封装当前总页数
		int totalPage = (int) Math.ceil(1.0*totalCount/currentCount);
		pageBean.setTotalPage(totalPage);
		//5.封装当前页显示的数据
		int index = (currentPage-1)*currentCount;
		List<Product> list = dao.getList(cid,index,currentCount);
		pageBean.setList(list);
		//System.out.println("手动断点"+list);
		
		
		//返回当前页
		return pageBean;
	}


	public Product getProductByPid(String pid) throws SQLException {
		ProductDao dao = new ProductDao();
		Product product = dao.getProductByPid(pid);
		return product;
	}


	public void submitOrder(Order order) {
		ProductDao dao = new ProductDao();
		try {
			//开启事务
			DataSourceUtils.startTransaction();
			//调用dao存储order表数据的方法
			dao.addOrders(order);
			//调用dao存储orderItem表数据的方法
			dao.addOrderItem(order);
		} catch (SQLException e) {
			//回调
			try {
				DataSourceUtils.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}finally {
			try {
				DataSourceUtils.commitAndRelease();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}


	public void updateOrderAdrr(Order order) {
		ProductDao dao = new ProductDao();
		try {
			dao.updateOrderAdrr(order);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}


	public void updateOrderState(String r6_Order) {
		ProductDao dao = new ProductDao();
		try {
			dao.updateOrderState(r6_Order);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}


	public List<Order> findAllOrders(String uid) {
		ProductDao dao = new ProductDao();
		List<Order> orderList = null;
		try {
			orderList = dao.findAllOrders(uid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return orderList;
	}


	public List<Map<String, Object>> findAllOrderItemByOid(String oid) {
		ProductDao dao =new ProductDao();
		List<Map<String, Object>> mapList = null;
		try {
			mapList = dao.findAllOrderItemByOid(oid);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mapList;
	}


}
