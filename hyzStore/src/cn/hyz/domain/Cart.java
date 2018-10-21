package cn.hyz.domain;

import java.util.HashMap;
import java.util.Map;

public class Cart {
	
	//封装购物车里的所有每一项订单
	private Map<String,CartItem> cartItems = new HashMap<String,CartItem>();
	//商品的总计
	private double total;
	
	public Map<String, CartItem> getCartItems() {
		return cartItems;
	}
	public void setCartItems(Map<String, CartItem> cartItems) {
		this.cartItems = cartItems;
	}
	public double getTotal() {
		return total;
	}
	public void setTotal(double total) {
		this.total = total;
	}
	
	

}
