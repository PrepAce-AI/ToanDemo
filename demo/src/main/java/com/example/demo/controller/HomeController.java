package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.Optional;

@Controller
public class HomeController {

    // Đây chính là dòng bạn đang thiếu để sửa lỗi chữ đỏ
    @Autowired
    private UserRepository userRepository;

    // 1. Dành cho Guest (và cả người đã đăng nhập) - Xem Trang Chủ
    @GetMapping("/")
    public String indexPage(Principal principal, Model model) {
        if (principal != null) {
            String email = "";
            String name = "";
            String avatar = "";

            // Kiểm tra xem là đăng nhập Google hay Đăng nhập thường
            if (principal instanceof OAuth2AuthenticationToken) {
                OAuth2User oauth2User = ((OAuth2AuthenticationToken) principal).getPrincipal();
                email = oauth2User.getAttribute("email");
                name = oauth2User.getAttribute("name"); // Lấy tên từ Google
                avatar = oauth2User.getAttribute("picture"); // Lấy ảnh từ Google
            } else {
                email = principal.getName();
            }

            // Tìm trong Database để lấy dữ liệu chính xác nhất
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                name = user.getFullName() != null ? user.getFullName() : name;
                avatar = user.getAvatarUrl() != null ? user.getAvatarUrl() : avatar;
            }

            // Đẩy tên và ảnh sang giao diện HTML
            model.addAttribute("userFullName", name != null ? name : email);
            model.addAttribute("userAvatar", avatar != null ? avatar : "https://cdn-icons-png.flaticon.com/512/149/149071.png");
        }
        return "index";
    }

    // 2. Dành cho Guest (và cả người đã đăng nhập) - Xem Giới thiệu khóa học
    @GetMapping("/course-info/{subject}")
    public String courseInfoPage(@PathVariable String subject, Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("userFullName", principal.getName());
        }
        model.addAttribute("subject", subject.toUpperCase());
        return "course-info";
    }

    // 3. CHỈ dành cho Học viên (Đã đăng nhập) - Vào phòng học xem Video
    @GetMapping("/course-learn/{subject}")
    public String courseLearnPage(@PathVariable String subject, Model model) {
        model.addAttribute("subject", subject.toUpperCase());
        return "course-detail";
    }
}