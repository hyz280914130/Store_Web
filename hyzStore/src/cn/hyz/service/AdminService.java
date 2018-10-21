package cn.hyz.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import cn.hyz.dao.AdminDao;
import cn.hyz.domain.Category;
import cn.hyz.domain.Order;
import cn.hyz.domain.Product;

public class AdminService {

	public List<Category> findAllCategory() {
		
		AdminDao dao = new AdminDao();
		List<Category> category = null;
		try {
			category = dao.findAllCategory();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//System.out.println(category);
		return category;
	}

	public void saveProduct(Product product) {
		
		AdminDao dao = new AdminDao();
		try {
			dao.saveProduct(product);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<Order> findAllOrders() {
		AdminDao dao = new AdminDao();
		List<Order> orderList = null;
		try {
			orderList = dao.findAllOrders();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return orderList;
	}

	public List<Map<String, Object>> findOrderInfoByOid(String oid) {
		AdminDao dao = new AdminDao();
		List<Map<String, Object>> map = null;
		try {
			map = dao.findOrderInfoByOid(oid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return map;
	}

}
