package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. XỬ LÝ LƯU HỒ SƠ & ẢNH ĐẠI DIỆN
    @PostMapping("/profile/edit")
    public String updateProfile(@RequestParam("fullName") String fullName,
                                @RequestParam("avatarUrl") String avatarUrl,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        Optional<User> userOpt = userRepository.findByEmail(principal.getName());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFullName(fullName); // Đổi tên
            user.setAvatarUrl(avatarUrl); // Đổi link ảnh
            userRepository.save(user); // Lưu vào Database

            redirectAttributes.addFlashAttribute("success", "Cập nhật hồ sơ và ảnh đại diện thành công!");
        }
        return "redirect:/profile#tab-profile"; // Load lại trang và mở tab hồ sơ
    }

    // 2. XỬ LÝ ĐỔI MẬT KHẨU
    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        Optional<User> userOpt = userRepository.findByEmail(principal.getName());
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Kiểm tra mật khẩu cũ có khớp trong database không
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("pw_error", "Mật khẩu hiện tại không chính xác!");
                return "redirect:/profile#tab-security";
            }

            // Mã hóa và lưu mật khẩu mới
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("pw_success", "Đổi mật khẩu thành công!");
        }
        return "redirect:/profile#tab-security"; // Load lại trang và mở tab bảo mật
    }
}