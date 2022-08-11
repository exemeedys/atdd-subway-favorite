package nextstep.auth.authentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import nextstep.auth.UserDetailsService;
import nextstep.auth.context.Authentication;
import nextstep.auth.context.SecurityContextHolder;
import nextstep.member.application.LoginMemberService;
import nextstep.member.domain.LoginMember;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public abstract class AbstractCreateAuthenticationFilter implements HandlerInterceptor {

    protected UserDetailsService userDetailsService;

    public AbstractCreateAuthenticationFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    protected abstract AuthenticationToken getAuthenticationToken(HttpServletRequest request) throws IOException;

    protected abstract String returnAuthenticationToken(String principal, List<String> authorities) throws JsonProcessingException;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        AuthenticationToken authenticationToken = getAuthenticationToken(request);

        LoginMember loginMember = userDetailsService.loadUserByUsername(authenticationToken.getPrincipal());

        if(loginMember == null) {
            throw new AuthenticationException();
        }

        if (!loginMember.checkPassword(authenticationToken.getCredentials())) {
            throw new AuthenticationException();
        }

        String responseToClient = returnAuthenticationToken(loginMember.getEmail(), loginMember.getAuthorities());
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getOutputStream().print(responseToClient);
        return false;
    }

}
