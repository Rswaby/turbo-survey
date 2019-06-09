package com.rohanswaby.surveyJunky.security;


import com.rohanswaby.surveyJunky.models.User;
import com.rohanswaby.surveyJunky.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails  loadUserByUsername(String usernameOrEmail)throws UsernameNotFoundException {

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail,usernameOrEmail).orElseThrow(
                ()-> new UsernameNotFoundException("Username or Email not found"+usernameOrEmail)
                );

        return UserPrincipal.createUser(user);

    }

    @Transactional
    public UserDetails loadUserById(Long id){
        User user = userRepository.findById(id).orElseThrow(
                () -> new UsernameNotFoundException("User not found with id:"+id)
        );
        return UserPrincipal.createUser(user);
    }
}
