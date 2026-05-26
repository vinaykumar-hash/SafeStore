package com.vaultforge.service;

import com.vaultforge.domain.User;
import com.vaultforge.exception.NotFoundException;
import com.vaultforge.repository.UserRepository;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User save(User user) {
    return userRepository.save(user);
  }

  public User getByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundException("User not found"));
  }

  public UserDetails loadUserByUsername(String username) {
    User user = getByEmail(username);
    return new org.springframework.security.core.userdetails.User(
        user.getEmail(), user.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
  }
}
