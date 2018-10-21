package cn.hyz.service;

import java.sql.SQLException;

import cn.hyz.dao.UserDao;
import cn.hyz.domain.User;

public class UserService {

public boolean regist(User user) {
		
		UserDao dao = new UserDao();
		int row = 0;
		try {
			row = dao.regist(user);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return row>0?true:false;
	}

public void active(String activeCode) throws SQLException {
	
		UserDao dao = new UserDao();
		dao.active(activeCode);
	
	}

public boolean checkUsername(String username) throws SQLException {
	UserDao dao =new UserDao();
	Long rows = dao.checkUsername(username);
	return rows>0?true:false;
}

public User login(String username, String password) {
	UserDao dao = new UserDao();
	User user = null;
	try {
		user = dao.login(username,password);
	} catch (SQLException e) {
		e.printStackTrace();
	}
	return user;
}

}
