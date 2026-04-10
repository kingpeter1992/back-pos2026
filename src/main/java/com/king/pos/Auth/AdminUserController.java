package com.king.pos.Auth;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.king.pos.Dto.UserDto;
import com.king.pos.Entitys.ERole;
import com.king.pos.Entitys.User;
import com.king.pos.ImplementServices.UserServiceImpl;




@CrossOrigin("*")
@RestController
@RequestMapping("/api/admin")
public class AdminUserController {
    @Autowired private UserServiceImpl userService;

  @GetMapping
public List<UserDto> getAllUsers() {
  return userService.getAllUsers();
}

    @PutMapping("/{id}/block")
    public UserDto blockUser(@PathVariable Long id) {
        return userService.blockUser(id);
    }

    @PutMapping("/{id}/unblock")
    public UserDto unblockUser(@PathVariable Long id) {
        return userService.unblockUser(id);
    }

 @PutMapping("/{id}/roles")
@PreAuthorize("hasRole('ADMIN')")
public UserDto assignRoles(@PathVariable Long id, @RequestBody Set<String> roles) {
    return userService.assignRoles(id, roles);
}

}
 