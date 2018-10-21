package cn.hyz.web.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cn.hyz.domain.User;

public class UserLoginPrivilegeFilter implements Filter {

    public UserLoginPrivilegeFilter() {
       
    }

	public void destroy() {
		
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		//有session代表用户已经登陆了
		HttpSession session = req.getSession();
		User user = (User) session.getAttribute("user");
		//如果用户没登陆
		if(user==null) {
			resp.sendRedirect(req.getContextPath()+"/login.jsp");
			//虽然转发但是下面的代码还是会执行，我们要return，这样就不会执行下面这个循环
			return;
		}
		chain.doFilter(request, response);
	}

	public void init(FilterConfig fConfig) throws ServletException {
	
	}

}
