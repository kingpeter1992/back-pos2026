package com.king.pos.Dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.king.pos.Entitys.User;



@Repository
public interface UserRepository  extends JpaRepository<User, Long>{
	
	@Query(value="select * from app_user WHERE username like :username", nativeQuery =true)
	public Page<User> listUser(@Param("username") String mc, Pageable page);
	Optional<User> findByUsernameAndActiveTrue(String username);

	User findByusername(String username);
	
	public User findByemail(String email);
	
	@Query(value="select * from app_user WHERE username = :username", nativeQuery =true)
	public Optional<User> findById(@Param("username") String username);	
	
	
    Optional<User> findByUsername(String username);


	Boolean existsByUsername(String username);

	Boolean existsByEmail(String email);

    public Optional<User> findByEmail(String email);

    Optional<User> findByToken(String token);

  @Query("select distinct u from User u left join fetch u.roles")
  List<User> findAllWithRoles();
   @Query("select u from User u left join fetch u.roles where u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);


}
