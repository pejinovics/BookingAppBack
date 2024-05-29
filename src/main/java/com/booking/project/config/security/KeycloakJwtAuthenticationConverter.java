package com.booking.project.config.security;

import com.booking.project.model.enums.AdminPermission;
import com.booking.project.model.enums.GuestPermission;
import com.booking.project.model.enums.HostPermission;
import com.booking.project.model.enums.UserType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt source) {
        return new JwtAuthenticationToken(
                source,
                Stream.concat(
                                new JwtGrantedAuthoritiesConverter().convert(source).stream(),
                                extractResourceRoles(source).stream())
                        .collect(toSet()));
    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        var resourceAccess = new HashMap<>(jwt.getClaim("realm_access"));

        var roles = (List<String>) resourceAccess.get("roles");

        var permissions = getPermissions(roles);
        var mainPermissions = permissions.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.replace("-", "_")))
                .collect(toSet());
        return mainPermissions;
    }
    private List<String> getPermissions(List<String> roles){
        List<String> rolePermissions = new ArrayList<String>();


        for (var role : roles) {
            try {
                switch (UserType.valueOf(role)) {
                    case GUEST:
                        for (var permission : GuestPermission.values()) {
                            Collections.addAll(rolePermissions,UserType.GUEST.toString()+"_"+ permission.toString());
                        }
                        break;
                    case ADMIN:
                        for (var permission : AdminPermission.values()) {
                            Collections.addAll(rolePermissions, UserType.ADMIN.toString()+"_"+permission.toString());
                        }
                        break;
                    case HOST:
                        for (var permission : HostPermission.values()) {
                            Collections.addAll(rolePermissions,UserType.HOST.toString()+"_" +permission.toString());
                        }
                        break;
                    default:
                        break;
                }
            }catch (IllegalArgumentException e){
                System.out.println("");
            }

        }
        return rolePermissions;
    }

}