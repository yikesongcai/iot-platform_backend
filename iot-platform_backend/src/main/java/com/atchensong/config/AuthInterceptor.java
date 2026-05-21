package com.atchensong.config;

import com.atchensong.common.BaseContext;
import com.atchensong.common.JwtUtil;
import com.atchensong.common.RequireAuth;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (JwtUtil.isTokenValid(token)) {
                Claims claims = JwtUtil.parseToken(token);
                BaseContext.setCurrentId(Long.valueOf(claims.getSubject()));
                BaseContext.setCurrentRole(claims.get("role", String.class));
            }
        }

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RequireAuth requireAuth = handlerMethod.getMethodAnnotation(RequireAuth.class);
            if (requireAuth != null && BaseContext.isGuest()) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(403);
                response.getWriter().write("{\"code\":1,\"msg\":\"游客无权执行此操作，请先登录\"}");
                return false;
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        BaseContext.clear();
    }
}
