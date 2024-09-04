package site.hesil.latteve_spring.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 *packageName    : site.hesil.latteve_spring.global.security
 * fileName       : TokenAuthenticationFilter
 * author         : yunbin
 * date           : 2024-08-29
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-08-29           yunbin           최초 생성
 * 2024-09-30           yunbin           access token 만료시 재발급된 token이 쿠키에 저장 안되는 문제 해결
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.info("토큰 검증 필터");
        String accessToken = resolveToken(request);

        // accessToken 검증
        if (tokenProvider.validateToken(accessToken)) {
            setAuthentication(accessToken);
            log.info("토큰 검증 완료");
        } else {
            // 만료되었을 경우 accessToken 재발급
            String reissueAccessToken = tokenProvider.reissueAccessToken(accessToken);
            log.info("재발급 토큰 " + reissueAccessToken);

            if (StringUtils.hasText(reissueAccessToken)) {
                setAuthentication(reissueAccessToken);

                // 재발급된 accessToken 다시 전달
                tokenProvider.addJwtCookieToResponse(response, reissueAccessToken);
                log.info("토큰 재발급 완료");
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String accessToken) {
        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String resolveToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) { // 쿠키 이름을 "jwt"로 가정
                    String cookieToken = cookie.getValue();
                    if (StringUtils.hasText(cookieToken)) {
                        log.info("resolveToken {}",  cookieToken);
                        return cookieToken;
                    }
                }
            }
        }
        log.info("토큰전달안됨 {}", request.getRequestURI());
        return null;
    }
}