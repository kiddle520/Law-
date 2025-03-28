package com.example.controller;

import com.example.service.UserService;
import com.example.entity.User;
import com.example.exception.UserNotFoundException;
import com.example.exception.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class BlacklistController {

    private static final Logger logger = LoggerFactory.getLogger(BlacklistController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/blacklist")
    public String showBlacklistPage(Model model, RedirectAttributes redirectAttributes) {
        try {
            List<User> normalUsers = userService.findNormalUsers();
            List<User> blacklistedUsers = userService.findBlacklistedUsers();

            model.addAttribute("normalUsers", normalUsers);
            model.addAttribute("blacklistedUsers", blacklistedUsers);

            return "admin/blacklist";
        } catch (DatabaseException e) {
            logger.error("获取用户列表失败: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "获取用户列表失败，请稍后重试");
            return "redirect:/admin/dashboard";
        } catch (AccessDeniedException e) {
            logger.error("访问被拒绝: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "您没有权限访问此页面");
            return "redirect:/";
        }
    }

    @PostMapping("/users/{id}/blacklist")
    @ResponseBody
    public String addToBlacklist(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.addToBlacklist(id);
            logger.info("User {} has been added to blacklist", id);
            redirectAttributes.addFlashAttribute("success", "用户已成功加入黑名单");
            return "success";
        } catch (UserNotFoundException e) {
            logger.error("Failed to add user {} to blacklist: {}", id, e.getMessage());
            return "error: " + e.getMessage();
        } catch (IllegalStateException e) {
            logger.error("Failed to add user {} to blacklist: {}", id, e.getMessage());
            return "error: " + e.getMessage();
        }
    }

    @PostMapping("/users/{id}/unblacklist")
    @ResponseBody
    public String removeFromBlacklist(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.removeFromBlacklist(id);
            logger.info("User {} has been removed from blacklist", id);
            redirectAttributes.addFlashAttribute("success", "用户已成功从黑名单中移除");
            return "success";
        } catch (UserNotFoundException e) {
            logger.error("Failed to remove user {} from blacklist: {}", id, e.getMessage());
            return "error: " + e.getMessage();
        } catch (IllegalStateException e) {
            logger.error("Failed to remove user {} from blacklist: {}", id, e.getMessage());
            return "error: " + e.getMessage();
        }
    }

    @ExceptionHandler({Exception.class})
    public String handleException(Exception e, RedirectAttributes redirectAttributes) {
        if (e instanceof AccessDeniedException) {
            logger.error("访问被拒绝: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "您没有权限执行此操作");
            return "redirect:/";
        } else if (e instanceof DatabaseException) {
            logger.error("数据库操作失败: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "系统错误，请稍后重试");
        } else {
            logger.error("未预期的错误: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "操作失败，请稍后重试");
        }
        return "redirect:/admin/blacklist";
    }
}