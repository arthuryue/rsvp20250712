package hk.jud.app.lyo.controller;

import hk.jud.app.lyo.service.JwtService;
import hk.jud.app.lyo.service.UserService;
import lombok.RequiredArgsConstructor;
import hk.jud.app.lyo.entity.Apiuser;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    

    @PostMapping("/login")
    public ResponseEntity<String> authenticate(@RequestBody Apiuser user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        
        if (authentication.isAuthenticated()) {
            String token = jwtService.generateToken(user.getUsername());
            return ResponseEntity.ok(token);
        }
        throw new UsernameNotFoundException("Invalid credentials");
    }
    
    

    @PostMapping("/register")
    public ResponseEntity<Apiuser> register(@RequestBody Apiuser user) {
        return ResponseEntity.ok(userService.createUser(user));
    }
}