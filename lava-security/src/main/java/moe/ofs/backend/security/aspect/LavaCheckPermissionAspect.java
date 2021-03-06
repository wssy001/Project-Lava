package moe.ofs.backend.security.aspect;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.ofs.backend.domain.LavaUserToken;
import moe.ofs.backend.domain.Permission;
import moe.ofs.backend.dto.AdminInfoDto;
import moe.ofs.backend.security.annotation.CheckPermission;
import moe.ofs.backend.security.exception.authorization.InsufficientAccessRightException;
import moe.ofs.backend.security.exception.token.AccessTokenExpiredException;
import moe.ofs.backend.security.exception.token.InvalidAccessTokenException;
import moe.ofs.backend.security.provider.PasswordTypeProvider;
import moe.ofs.backend.security.service.AccessTokenService;
import moe.ofs.backend.security.service.AdminInfoService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LavaCheckPermissionAspect {
    private final AccessTokenService accessTokenService;
    private final AdminInfoService adminInfoService;
    private final PasswordTypeProvider passwordTypeProvider;

    @Pointcut("@annotation(moe.ofs.backend.security.annotation.CheckPermission)")
    public void annotatedMethods() {
    }

    @Pointcut("@within(moe.ofs.backend.security.annotation.CheckPermission)")
    public void annotatedClasses() {
    }

    @Before("annotatedClasses() || annotatedMethods()")
    public Object checkPermission(JoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        CheckPermission methodAnnotation = signature.getMethod().getAnnotation(CheckPermission.class);
        Class<?> aClass = point.getSignature().getDeclaringType();
        CheckPermission classAnnotation = aClass.getAnnotation(CheckPermission.class);
        Permission permission = getCheckPermission(methodAnnotation, classAnnotation);

        Set<String> groups = permission.getGroups();
        Set<String> nonGroups = permission.getNonGroups();
        Set<String> roles = permission.getRoles();
        Set<String> nonRoles = permission.getNonRoles();
        if (ObjectUtil.isAllEmpty(groups, nonGroups, roles, nonGroups)) return point.getArgs();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        if (permission.isRequiredAccessToken()) {
            HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            String accessToken = request.getHeader("access_token");
            if (accessToken == null) throw new InvalidAccessTokenException("accessToken不能为空！");

//            获取token中存储的信息，调用provider校验信息
            boolean b = accessTokenService.checkAccessToken(accessToken);
            if (!b) throw new AccessTokenExpiredException("accessToken已过期，请使用refreshToken刷新");
            LavaUserToken lavaUserToken = accessTokenService.getByAccessToken(accessToken);
            Authentication token = (Authentication) lavaUserToken.getUserInfoToken();

            // 判断是否重新认证用户信息
            if (!authentication.getName().equals(token.getName())) {
                Authentication authenticate = passwordTypeProvider.authenticate(accessToken);
//            将用户认证信息存入session
                SecurityContextHolder.getContext().setAuthentication(authenticate);
            }
            username = token.getName();
        }

        if (username.equals("anonymousUser")) throw new InsufficientAccessRightException("请先登录！");
        AdminInfoDto adminInfoDto = adminInfoService.getOneByName(username);

        boolean inAllowedGroups, hasAllowedRoles;
        inAllowedGroups = checkArray(adminInfoDto.getGroups(), groups, nonGroups);
        hasAllowedRoles = checkArray(adminInfoDto.getRoles(), roles, nonRoles);

        if (inAllowedGroups && hasAllowedRoles) {
            return point.getArgs();
        } else {
            throw new InsufficientAccessRightException("无权访问！");
        }
    }

    private Permission getCheckPermission(CheckPermission methodAnnotation, CheckPermission classAnnotation) {
        Permission permission = new Permission();
        CheckPermission check = methodAnnotation != null ? methodAnnotation : classAnnotation;
        if (check != null) {
            permission.getGroups().addAll(Arrays.asList(classAnnotation.groups()));
            permission.getRoles().addAll(Arrays.asList(classAnnotation.roles()));
            permission.getNonGroups().addAll(Arrays.asList(classAnnotation.nonGroups()));
            permission.getNonRoles().addAll(Arrays.asList(classAnnotation.nonRoles()));
        }
        return permission;

    }

    private boolean checkArray(List<String> test, Set<String> exist, Set<String> noExist) {
        if (noExist.isEmpty() && exist.isEmpty()) return true;
        if (test.isEmpty()) return exist.isEmpty();

//        test  noExist不为空
        if (ArrayUtil.isEmpty(exist)) {
            return checkNoExist(test, noExist);
        }
//        test  exist不为空
        if (ArrayUtil.isEmpty(noExist)) {
            return checkExist(test, exist);
        }

//        仨不为空
        boolean flag1, flag2;
        flag1 = checkExist(test, exist);
        flag2 = checkNoExist(test, noExist);
        return flag1 && flag2;
    }

    private boolean checkNoExist(List<String> test, Set<String> noExist) {
        Set<String> a = new HashSet<>(test);
        a.addAll(test);
        return a.size() == (test.size() + noExist.size());
    }

    private boolean checkExist(List<String> test, Set<String> exist) {
        if (test.size() < exist.size()) return false;
        Set<String> a = new HashSet<>(test);
        Set<String> b = new HashSet<>(exist);
        b.retainAll(a);
        return b.size() == exist.size();
    }

}
