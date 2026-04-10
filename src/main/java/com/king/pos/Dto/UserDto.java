package com.king.pos.Dto;

import java.util.List;

import com.king.pos.Entitys.ERole;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter
public class UserDto{
        public UserDto(Long id2, String username2, String email2, boolean b, List<ERole> list) {
        //TODO Auto-generated constructor stub
        this.email=email2;
        this.id=id2;
        this.username=username2;
        this.active=b;
        this.roles=list.stream().map(ERole::name).toList();
    }
       
        Long id;
        String username;
        String email;
        List<String> roles;
        boolean active;
        public UserDto() {
        }
 
}