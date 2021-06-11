/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thiendz.j5.assignment.controller.admin;

import java.io.File;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import thiendz.j5.assignment.dao.AccountDAO;
import thiendz.j5.assignment.model.Account;
import thiendz.j5.assignment.service.ErrorManager;
import thiendz.j5.assignment.service.ParamService;
import thiendz.j5.assignment.util.Utils;

@Controller
@RequestMapping("/admin/account-manager")
public class AccountManagerController {

    private static final String PATH_SAVE_ACCOUNT_IMG = "/assets/img/account/";
    @Autowired
    HttpServletRequest rq;
    @Autowired
    ErrorManager errorManager;
    @Autowired
    AccountDAO accountDAO;
    @Autowired
    ParamService paramService;

    @GetMapping
    public String getIndex(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "col-sort", defaultValue = "time") String colSort,
            @RequestParam(name = "type-sort", defaultValue = "DESC") String typeSort,
            @RequestParam(name = "edit", defaultValue = "") String edit
    ) {
        Account account = new Account();
        if (!edit.equals("")) {
            account = accountDAO.findById(edit).get();
        }
        Page<Account> listAccounts = accountDAO.findAll(PageRequest.of(page, size, Sort.Direction.valueOf(typeSort), colSort));
        rq.setAttribute("listAccounts", listAccounts.getContent());
        rq.setAttribute("accountForm", account);
        rq.setAttribute("page", page);
        rq.setAttribute("typeSort", typeSort.equals("DESC") ? "ASC" : "DESC");
        return "/admin/account-manager";
    }

    @GetMapping("/add")
    public String toIndex() {
        return "redirect:/admin/account-manager";
    }

    @RequestMapping("/add")
    public String updateAccount(
            @Valid @ModelAttribute("accountForm") Account account,
            BindingResult bindingResult,
            @RequestParam("file") MultipartFile multipartFile
    ) {
        errorManager.start("/admin/account-manager");
        if (bindingResult.hasErrors()) {
            errorManager.add("form not valid!");
            return errorManager.path();
        }
        String type = "Thêm thành công!";
        Account accountDB = accountDAO.getById(account.getUsername());
        if (accountDB == null) { // thêm mới
            String path = "";
            if (!multipartFile.isEmpty()) {
                errorManager = saveImg(multipartFile, paramService, errorManager);
                if (errorManager.exists()) {
                    return errorManager.path();
                }
                account.setPhoto(errorManager.success());
            }
        } else { // sửa
            if (!multipartFile.isEmpty()) {
                errorManager = saveImg(multipartFile, paramService, errorManager);
                if (errorManager.exists()) {
                    return errorManager.path();
                }
                account.setPhoto(errorManager.success());
            }
            type = "Sửa thành công!";
        }
        accountDAO.save(account);
        errorManager.success(type);
        return errorManager.path();
    }

    public static ErrorManager saveImg(MultipartFile multipartFile, ParamService paramService, ErrorManager errorManager) {
        if (multipartFile.isEmpty()) {
            errorManager.add("file is empty!");
            return errorManager;
        }
        if (!multipartFile.getOriginalFilename().endsWith(".png")) {
            errorManager.add("Chỉ hỗ trợ ảnh png!");
            return errorManager;
        }
        String name = "avatar_" + Utils.random.nextInt(999999) + ".png";
        File result = paramService.save(multipartFile, PATH_SAVE_ACCOUNT_IMG, name);
        if (result == null) {
            errorManager.add("lưu ảnh vào db thất bại!");
            return errorManager;
        }
        errorManager.success(PATH_SAVE_ACCOUNT_IMG + name);
        return errorManager;
    }
}