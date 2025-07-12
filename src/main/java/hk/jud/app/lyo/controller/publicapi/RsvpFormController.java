package hk.jud.app.lyo.controller.publicapi;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/rsvp-form")
@RequiredArgsConstructor
public class RsvpFormController {

	//@GetMapping("/sc/{even_id}/{guset_id}")
    //public String rsvpForm(@PathVariable Integer id, @PathVariable String guset_id) {
	@GetMapping("/sc")
	    public String rsvpForm() {	
        return "rsvp-form";
    }
	@GetMapping("/thank-you")
    public String thankyou() {	
    return "thank-you";

	}
    
  
    
}