package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes, Model model) {
        try {
            // 1. MÃ HÓA MẬT KHẨU
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);

            // 2. THÊM 2 DÒNG NÀY ĐỂ MỞ KHÓA TÀI KHOẢN
            user.setEnabled(true); // Bật kích hoạt tài khoản để cho phép đăng nhập
            user.setProvider("LOCAL"); // Đánh dấu là tài khoản đăng ký thường

            // 3. LƯU USER VÀO DATABASE
            userRepository.save(user);

            // 4. CHUYỂN HƯỚNG SANG TRANG LOGIN
            redirectAttributes.addFlashAttribute("success", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
            return "redirect:/login";

        } catch (Exception e) {
            model.addAttribute("error", "Đăng ký thất bại: Email đã được sử dụng!");
            return "register";
        }
    }

    @GetMapping("/verify")
    public String verifyUser(@RequestParam("token") String token, Model model) {
        if (userService.verify(token)) {
            model.addAttribute("success", "Tài khoản đã kích hoạt! Bạn có thể đăng nhập.");
        } else {
            model.addAttribute("error", "Token kích hoạt không đúng hoặc đã hết hạn.");
        }
        return "login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, HttpServletRequest request, Model model) {
        String siteUrl = request.getRequestURL().toString().replace(request.getServletPath(), "");
        if (userService.sendForgotPasswordToken(email, siteUrl)) {
            model.addAttribute("success", "Link đặt lại mật khẩu đã gửi vào email của bạn.");
        } else {
            model.addAttribute("error", "Email không tồn tại hoặc đăng ký bằng tài khoản Google.");
        }
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token, @RequestParam("password") String password, Model model) {
        if (userService.resetPassword(token, password)) {
            model.addAttribute("success", "Đổi mật khẩu thành công! Hãy đăng nhập lại.");
            return "login";
        } else {
            model.addAttribute("error", "Đường dẫn không hợp lệ.");
            return "reset-password";
        }
    }

    @GetMapping("/profile")
    public String profilePage(Principal principal, Model model) {
        String email = getEmailFromPrincipal(principal);
        User user = userRepository.findByEmail(email).orElseThrow();
        model.addAttribute("user", user);
        return "profile";
    }

//    @PostMapping("/profile/edit")
//    public String editProfile(Principal principal, @RequestParam("fullName") String fullName, @RequestParam("avatarUrl") String avatarUrl) {
//        String email = getEmailFromPrincipal(principal);
//        userService.updateProfile(email, fullName, avatarUrl);
//        return "redirect:/profile?success";
//    }

//    @PostMapping("/profile/change-password")
//    public String changePassword(Principal principal, @RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, RedirectAttributes redirectAttributes) {
//        String email = getEmailFromPrincipal(principal);
//        if (userService.changePassword(email, oldPassword, newPassword)) {
//            return "redirect:/profile?pw_success";
//        } else {
//            return "redirect:/profile?pw_error";
//        }
//    }

    private String getEmailFromPrincipal(Principal principal) {
        if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            OAuth2User oauth2User = (OAuth2User) ((org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal).getPrincipal();
            return oauth2User.getAttribute("email");
        }
        return principal.getName();
    }
}