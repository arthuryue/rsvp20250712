package hk.jud.app.lyo.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import hk.jud.app.lyo.dto.CustomResponse;
import lombok.RequiredArgsConstructor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component("customRateLimiterAspect")
@RequiredArgsConstructor
public class RateLimiterAspect {

	private final RateLimiter rateLimiter;

	@Around("execution(public * hk.jud.app.lyo.controller..*(..))  "
			//+ "@annotation(org.springframework.web.bind.annotation.RequestMapping) && "
			+ "&& within(@org.springframework.web.bind.annotation.RestController *)"
			//+ "&& within(@org.springframework.web.bind.annotation.Controller *)"
			)
			
	public Object applyRateLimiter(ProceedingJoinPoint joinPoint) throws Throwable {
		try {
			return rateLimiter.executeSupplier(() -> {
				try {
					return joinPoint.proceed();
				} catch (Throwable t) {
					throw new RuntimeException(t);
				}
			});
		} catch (RequestNotPermitted e) {
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
					.body(new CustomResponse<>("ERROR", "Rate limit exceeded. Try again later.", null));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new CustomResponse<>("ERROR", e.getMessage(), null));
		}
	}
}