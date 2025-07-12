package hk.jud.app.lyo.service;

import hk.jud.app.lyo.entity.Apiuser;
import hk.jud.app.lyo.repository.ApiuserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final ApiuserRepository apiuserRepository;
    private final PasswordEncoder passwordEncoder;


    public List<Apiuser> getAllApiusers() {
        return apiuserRepository.findAll();
    }

    public Apiuser createUser(Apiuser apiuser) {
    	apiuser.setPassword(passwordEncoder.encode(apiuser.getPassword()));
        return apiuserRepository.save(apiuser);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    	Apiuser user = apiuserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    	return new User(user.getUsername(), user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole())));
    }
}