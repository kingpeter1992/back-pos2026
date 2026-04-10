package com.king.pos.ImplementServices;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.king.pos.Dao.*;
import com.king.pos.Dto.UserDto;
import com.king.pos.Entitys.*;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class UserServiceImpl {

    @Autowired private UserRepository userRepository;

    @Autowired private RoleRepository roleRepository;
  @Transactional
  public List<UserDto> getAllUsers() {
    return userRepository.findAllWithRoles().stream()
        .map(u -> new UserDto(
            u.getId(),
            u.getUsername(),
            u.getEmail(),
            u.isActive(),
            u.getRoles().stream().map(Role::getName).toList()
        ))
        .toList();
  }

  

    public UserDto blockUser(Long userId) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        user.setActive(false);
            User saved = userRepository.save(user);

           return new UserDto(
            saved.getId(),
            saved.getUsername(),
            saved.getEmail(),
            saved.isActive(),
            saved.getRoles().stream().map(Role::getName).toList()
    );
    }

    public UserDto unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        user.setActive(true);
        User saved = userRepository.save(user);

      return new UserDto(
            saved.getId(),
            saved.getUsername(),
            saved.getEmail(),
            saved.isActive(),
            saved.getRoles().stream().map(Role::getName).toList()
    );
    }

private Role getRoleOrThrow(ERole roleName) {
    return roleRepository.findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Rôle " + roleName + " introuvable"));
}
@Transactional
public UserDto assignRoles(Long userId, Set<String> roleNames) {
    User user = userRepository.findByIdWithRoles(userId)
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

    Set<Role> newRoles = roleNames.stream()
            .map(ERole::valueOf)
            .map(this::getRoleOrThrow)
            .collect(Collectors.toSet());

    user.setRoles(newRoles); // ✅ évite clear/addAll

    User saved = userRepository.save(user);

    return new UserDto(
            saved.getId(),
            saved.getUsername(),
            saved.getEmail(),
            saved.isActive(),
            saved.getRoles().stream().map(Role::getName).toList()
    );
}

}
