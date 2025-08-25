package com.handederelii.bom_project.service;

import com.handederelii.bom_project.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder ;
    private final Map<String, String> users = new ConcurrentHashMap<>();

    // 📌 Register fonksiyonu
    public UserResponse register(String email, String rawPassword) {
        // Şifreyi encode edelim
        String encoded = passwordEncoder.encode(rawPassword);
        users.put(email, encoded);
        // Normalde burada DB'ye kaydedersin, biz şimdilik sadece log atalım
        System.out.println("Saved user: " + email + " / " + encoded);

        return new UserResponse(email, "Kullanıcı başarıyla kaydedildi!");
    }

    // LOGIN doğrulaması (controller’da çağırdığın method)
    public void validateCredentials(String email, String rawPassword) {
        String storedHash = users.get(email);
        if (storedHash == null || !passwordEncoder.matches(rawPassword, storedHash)) {
            throw new IllegalArgumentException("bad credentials");
        }
    }
}
