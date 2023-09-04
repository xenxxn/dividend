package com.example.dividend.service;

import com.example.dividend.exception.impl.AlreadyExistUserException;
import com.example.dividend.exception.impl.NoExistUserIdException;
import com.example.dividend.exception.impl.NotMatchUserPasswordException;
import com.example.dividend.model.Auth;
import com.example.dividend.model.MemberEntity;
import com.example.dividend.persist.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("couldn't find user : " + username));
    }

    public MemberEntity register(Auth.SignUp member){
        boolean exists = this.memberRepository.existsByUsername(member.getUsername());
        if (exists) {
            throw new AlreadyExistUserException();
        }

        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        return this.memberRepository.save(member.toEntity());
    }

    public MemberEntity authenticate(Auth.SignIn member){
        var user = this.memberRepository.findByUsername(member.getUsername())
                .orElseThrow(() -> new NoExistUserIdException());
        if (!this.passwordEncoder.matches(member.getPassword(), user.getPassword())) {
            throw new NotMatchUserPasswordException();
        }
        return user;
    }
}
