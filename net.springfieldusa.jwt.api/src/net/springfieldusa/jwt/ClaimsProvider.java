package net.springfieldusa.jwt;

import java.security.Principal;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;

public interface ClaimsProvider
{
   void addClaims(Map<String, Object> claims, ContainerRequestContext context, Principal principal);
}
