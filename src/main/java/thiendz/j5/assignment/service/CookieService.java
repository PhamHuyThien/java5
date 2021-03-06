/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thiendz.j5.assignment.service;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import thiendz.j5.assignment.dao.AccountDAO;
import thiendz.j5.assignment.model.Account;

/**
 *
 * @author Administrator
 */
@Service
public class CookieService {

	@Autowired
	HttpServletRequest rq;
	@Autowired
	HttpServletResponse rp;
	@Autowired
	AccountDAO accountDAO;

	public Account getAccount() {
		Account account = null;
		String username = getValue("username");
		String password = getValue("password");
		if (username != null && password != null) {
			account = accountDAO.getAccount(username, password);
		}
		return account;
	}

	public Cookie get(String name) {
		Cookie[] cookies = rq.getCookies();
		Cookie result = null;
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					result = cookie;
					break;
				}
			}
		}
		return result;
	}

	public String getValue(String name) {
		Cookie cookie = this.get(name);
		return cookie == null ? null : cookie.getValue();
	}

	public void add(String name, String value, int hour) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(hour * 60 * 60);
		rp.addCookie(cookie);
	}

	public void remove(String name) {
		int len = rq.getCookies().length;
		for (Cookie cookie : rq.getCookies()) {
			if (cookie.getName().equalsIgnoreCase(name)) {
				Cookie c = cookie;
				cookie.setMaxAge(0);
				rp.addCookie(cookie);
				break;
			}
		}
	}

}
