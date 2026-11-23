package com.training.featuretoggle;



import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DbUserDetailsService implements UserDetailsService {

    private final AppUserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser u = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user: " + email));
        return User.withUsername(u.getEmail())
                .password(u.getPassword())
                .roles(u.getRole())      // ADMIN becomes ROLE_ADMIN
                .build();
    }
}
