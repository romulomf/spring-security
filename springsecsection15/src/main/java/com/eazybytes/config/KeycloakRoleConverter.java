package com.eazybytes.config;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import io.jsonwebtoken.lang.Collections;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

	@Override
	public Collection<GrantedAuthority> convert(Jwt source) {
		var realmAccess = source.getClaimAsMap("realm_access");
		if (realmAccess == null || realmAccess.isEmpty()) {
			return Collections.emptyList();
		}
		@SuppressWarnings("unchecked")
		List<String> roles = (List<String>) realmAccess.get("roles");
		if (Collections.isEmpty(roles)) {
			return Collections.emptyList();
		}
		return roles.stream().map("ROLE_"::concat).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	}
}