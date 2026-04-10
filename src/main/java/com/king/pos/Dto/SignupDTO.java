package com.king.pos.Dto;

import java.util.Set;

public class SignupDTO {
	  private String username;
	  private String email;
	  private String password;
	  private String phone;

	  private Set<String> role;

	  public String getUsername() {
		  return username;
	  }

	  public void setUsername(String username) {
		  this.username = username;
	  }

	  public String getEmail() {
		  return email;
	  }

	  public void setEmail(String email) {
		  this.email = email;
	  }

	  public String getPassword() {
		  return password;
	  }

	  public void setPassword(String password) {
		  this.password = password;
	  }

	  public String getPhone() {
		  return phone;
	  }

	  public void setPhone(String phone) {
		  this.phone = phone;
	  }

	  public Set<String> getRole() {
		  return role;
	  }

	  public void setRole(Set<String> role) {
		  this.role = role;
	  }

}
