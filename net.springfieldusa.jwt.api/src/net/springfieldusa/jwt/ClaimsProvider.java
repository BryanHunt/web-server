package net.springfieldusa.jwt;

import java.security.Principal;
import java.util.Map;

public interface ClaimsProvider
{
  Map<String, Object> getClaims(Principal principal);
}
