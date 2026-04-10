package com.king.pos.Entitys;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "\"user\"") // PostgreSQL requires double quotes
public class User {
	 @Id
	  @GeneratedValue(strategy = GenerationType.IDENTITY)
	  private Long id;

	  /**
	 *
	 */
	 @NotBlank
	  @Size(max = 20)
	  private String username;

	  @NotBlank
	  @Size(max = 50)
	  @Email
	  private String email;

	  @NotBlank
	  @Size(max = 120)
	  private String password;

	  private boolean active ; // Utilisateur actif par défaut

	    private String token;
		private LocalDateTime tokeexpire = LocalDateTime.now().plusHours(1); // 1h de validité
  
	  @ManyToMany(fetch = FetchType.LAZY)
	  @JoinTable(  name = "user_roles", 
	        joinColumns = @JoinColumn(name = "user_id"), 
	        inverseJoinColumns = @JoinColumn(name = "role_id"))
	  private Set<Role> roles = new HashSet<>();

		
		  public User() { }
		 

		
		  public User(String username, String email, String password, boolean isActive) { this.username =
		  username; this.email = email; this.password = password;
		this.active = isActive; }
		
		  
		  public Long getId() { return id; }
		  
		  public void setId(Long id) { this.id = id; }
		  
		  public String getUsername() { return username; }
		  
		  public void setUsername(String username) { this.username = username; }
		  
		  public String getEmail() { return email; }
		  
		  public void setEmail(String email) { this.email = email; }
		  
		  public String getPassword() { return password; }
		  
		  public void setPassword(String password) { this.password = password; }
		  
		  public Set<Role> getRoles() { return roles; }
		public void setActive(boolean active) { this.active = active; }
		public boolean isActive() { return active; }
		  
		  public void setRoles(Set<Role> roles) { this.roles = roles; }
		public String getResetToken() {
			return token;
		}
		public void setResetToken(String token) {
			this.token = token;
		}
		public LocalDateTime getTokenExpiration() {
			return tokeexpire;
		}
		public void setTokenExpiration(LocalDateTime tokeexpire) {
			this.tokeexpire = tokeexpire;
		}
		 
}
