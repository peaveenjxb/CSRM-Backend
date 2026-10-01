package com.csrm;

import com.csrm.dto.Enums;
import com.csrm.model.Resource;
import com.csrm.model.User;
import com.csrm.repository.ResourceRepository;
import com.csrm.repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class CsrmApplication {
    public static void main(String[] args) { SpringApplication.run(CsrmApplication.class, args); }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    // First run: create the admin account and a few sample resources
    @Bean
    CommandLineRunner seed(UserRepository users, ResourceRepository resources, PasswordEncoder enc,
                           @Value("${csrm.admin.username}") String adminName,
                           @Value("${csrm.admin.password}") String adminPass) {
        return args -> {
            if (!users.existsByRole(Enums.Role.ADMIN)) {
                User u = new User();
                u.username = adminName;
                u.password = enc.encode(adminPass);
                u.role = Enums.Role.ADMIN;
                u.status = Enums.UserStatus.APPROVED;
                users.save(u);
            }
            if (resources.count() == 0) {
                resources.saveAll(List.of(
                        new Resource("Classroom A101", "CLASSROOM", "Block A, Floor 1"),
                        new Resource("Computer Lab 1", "LAB", "Block B, Floor 2"),
                        new Resource("Locker L-12", "LOCKER", "Library corridor"),
                        new Resource("Projector P-3", "EQUIPMENT", "AV store room")));
            }
        };
    }
}
