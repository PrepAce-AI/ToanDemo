package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Tài khoản chưa được kích hoạt qua Email");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword() != null ? user.getPassword() : "",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Override
    public void register(User user, String siteUrl) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setProvider("LOCAL");
        user.setEnabled(true);
        user.setVerificationToken(UUID.randomUUID().toString());
        userRepository.save(user);

        sendVerificationEmail(user, siteUrl);
    }

    private void sendVerificationEmail(User user, String siteUrl) {
        String verifyUrl = siteUrl + "/verify?token=" + user.getVerificationToken();
        String content = "Chào " + user.getFullName() + ",<br>"
                + "Vui lòng click vào link bên dưới để kích hoạt tài khoản:<br>"
                + "<h3><a href=\"" + verifyUrl + "\">KÍCH HOẠT NGAY</a></h3>";
        sendEmail(user.getEmail(), "Xác thực tài khoản Demo Auth", content);
    }

    @Override
    public boolean verify(String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEnabled(true);
            user.setVerificationToken(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean sendForgotPasswordToken(String email, String siteUrl) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent() && "LOCAL".equals(userOpt.get().getProvider())) {
            User user = userOpt.get();
            user.setResetPasswordToken(UUID.randomUUID().toString());
            userRepository.save(user);

            String resetUrl = siteUrl + "/reset-password?token=" + user.getResetPasswordToken();
            String content = "Yêu cầu khôi phục mật khẩu:<br>"
                    + "<h3><a href=\"" + resetUrl + "\">ĐỔI MẬT KHẨU MỚI</a></h3>";
            sendEmail(user.getEmail(), "Quên mật khẩu Demo Auth", content);
            return true;
        }
        return false;
    }

    @Override
    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByResetPasswordToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetPasswordToken(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public void updateProfile(String email, String fullName, String avatarUrl) {
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setFullName(fullName);
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            user.setAvatarUrl(avatarUrl);
        }
        userRepository.save(user);
    }

    @Override
    public boolean changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email).orElseThrow();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    @Override
    public void processOAuthPostLogin(String email, String name) {
        Optional<User> existUser = userRepository.findByEmail(email);
        if (existUser.isEmpty()) {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setProvider("GOOGLE");
            newUser.setEnabled(true);
            newUser.setAvatarUrl("https://lh3.googleusercontent.com/a/default-user=s100");
            userRepository.save(newUser);
        }
    }

    private void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}