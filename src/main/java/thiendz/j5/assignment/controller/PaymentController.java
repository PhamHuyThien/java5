/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thiendz.j5.assignment.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import thiendz.j5.assignment.dao.OrderDAO;
import thiendz.j5.assignment.dao.OrderDetailDAO;
import thiendz.j5.assignment.dao.ProductDAO;
import thiendz.j5.assignment.model.Account;
import thiendz.j5.assignment.model.Order;
import thiendz.j5.assignment.model.OrderDetail;
import thiendz.j5.assignment.model.Product;
import thiendz.j5.assignment.model.atrributes.PaymentForm;
import thiendz.j5.assignment.service.ErrorManager;
import thiendz.j5.assignment.service.SessionService;
import thiendz.j5.assignment.service.ShoppingCartServiceImpl;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    SessionService sessionService;
    @Autowired
    ShoppingCartServiceImpl shoppingCartServiceImpl;
    @Autowired
    HttpServletRequest rq;
    @Autowired
    ErrorManager error;
    @Autowired
    ProductDAO productDAO;
    @Autowired
    OrderDetailDAO orderDetailDAO;
    @Autowired
    OrderDAO orderDAO;

    @GetMapping
    public String getIndex() {
        if (!sessionService.isLogin()) {
            return "redirect:/login";
        }
        if (shoppingCartServiceImpl.getCount() == 0) {
            return "redirect:/";
        }
        Account account = sessionService.get("account");
        PaymentForm paymentForm = new PaymentForm(account.getFullname(), "", "");
        rq.setAttribute("paymentForm", paymentForm);
        rq.setAttribute("listCarts", shoppingCartServiceImpl.get());
        rq.setAttribute("totalPayment", shoppingCartServiceImpl.totalPayment());
        return "/payment";
    }

    @GetMapping({"/add"})
    public String redirectIndex() {
        return "redirect:/payment";
    }

    @RequestMapping("/add")
    public String payment(
            @Valid @ModelAttribute("paymentForm") PaymentForm paymentForm,
            BindingResult bindingResult
    ) {
        if (!sessionService.isLogin()) {
            return "redirect:/login";
        }
        if (shoppingCartServiceImpl.getCount() == 0) {
            return "redirect:/";
        }
        error.start("/payment");
        if (bindingResult.hasErrors()) {
            error.add("form not valid!");
            return error.path();
        }
        Account account = sessionService.get("account");
        Order order = new Order();
        order.setAccount(account);
        order.setAndress(paymentForm.getAndress());
        order.setTime(new Date());
        orderDAO.save(order);
        List<OrderDetail> listOrderDetails = new ArrayList<>();
        shoppingCartServiceImpl.get().forEach((k, cart) -> {
            Product product = productDAO.getById(cart.getId());
            listOrderDetails.add(new OrderDetail(
                    null,
                    order,
                    product,
                    cart.getPrice(),
                    cart.getQty()
            ));
        });
        listOrderDetails.forEach((orderDetail) -> {
            orderDetailDAO.save(orderDetail);
        });
        shoppingCartServiceImpl.clear();
        return "redirect:/";
    }
}
