package hk.jud.app.lyo.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;


@ControllerAdvice(basePackages = "hk.jud.app.lyo.controller.admin")
public class GlobalAdminControllerAdvice {

    @ModelAttribute
    public void addRequestURI(HttpServletRequest request, org.springframework.ui.Model model) {
        String uri = request.getRequestURI();
        if (request.getAttribute("jakarta.servlet.forward.request_uri") != null) {
            uri = (String) request.getAttribute("jakarta.servlet.forward.request_uri");
        }
        model.addAttribute("requestURI", uri);
        // Add username to model
        String username = getCurrentUsername();
        model.addAttribute("username", username);
        
    }
    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
    
}