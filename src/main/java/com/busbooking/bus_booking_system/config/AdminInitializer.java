package com.busbooking.bus_booking_system.config;

import com.busbooking.bus_booking_system.entity.Role;
import com.busbooking.bus_booking_system.entity.User;
import com.busbooking.bus_booking_system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    @Bean
    CommandLineRunner initializeAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Environment environment
    ) {
        return args -> {

            String adminEmail = environment.getProperty("ADMIN_EMAIL");
            String adminPassword = environment.getProperty("ADMIN_PASSWORD");

            if (adminEmail == null || adminPassword == null) {
                System.out.println("⚠️ ADMIN_EMAIL or ADMIN_PASSWORD not configured.");
                return;
            }

            userRepository.findByEmail(adminEmail)
                    .ifPresentOrElse(
                            user -> System.out.println("ℹ️ Admin already exists."),
                            () -> {
                                User admin = new User();
                                admin.setName("System Admin");
                                admin.setEmail(adminEmail);
                                admin.setPassword(passwordEncoder.encode(adminPassword));
                                admin.setRole(Role.ROLE_ADMIN);

                                userRepository.save(admin);
                                System.out.println("✅ Production admin created.");
                            }
                    );
        };
    }
}
