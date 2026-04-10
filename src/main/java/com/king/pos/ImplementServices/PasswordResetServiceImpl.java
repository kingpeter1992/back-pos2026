package com.king.pos.ImplementServices;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.king.pos.Dao.*;
import com.king.pos.Entitys.*;

@Transactional
@Service
public class PasswordResetServiceImpl {
  @Autowired   private UserRepository userRepository;
  private static final String BASIC_URL_FORGOT = "http://localhost:4200";
  @Autowired private EmailServiceImpl emailService;
  public void sendResetLink(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

     //   System.out.println("Received reponse: " + user.getEmail() + " " + user.getUsername()  + " " + user.getId()  );

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        
        user.setTokenExpiration(LocalDateTime.now().plusMinutes(30)); // 30 minutes validity
       

        userRepository.save(user);
      // System.err.println("Generated reset token: " + user.getResetToken() + " for user: " + user.getUsername());
     //  System.err.println("Token expiration time: " + user.getTokenExpiration());


        String resetLink = BASIC_URL_FORGOT + "/reset-password?token=" + token;
    //    System.out.println("Reset link: " + resetLink);

       emailService.sendEmail(email, "Password Reset",
    "Click here to reset your password: " + resetLink);
  }
    
}
