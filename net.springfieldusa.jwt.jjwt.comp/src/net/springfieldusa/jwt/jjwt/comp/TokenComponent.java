/*******************************************************************************
 * Copyright (c) 2016 Bryan Hunt.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bryan Hunt - initial API and implementation
 *******************************************************************************/

package net.springfieldusa.jwt.jjwt.comp;

import java.security.Key;
import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.container.ContainerRequestContext;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.LogService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import net.springfieldusa.comp.AbstractComponent;
import net.springfieldusa.credentials.UnencryptedCredential;
import net.springfieldusa.jwt.ClaimsProvider;
import net.springfieldusa.jwt.EncryptionSecretProvider;
import net.springfieldusa.jwt.TokenException;
import net.springfieldusa.jwt.TokenService;
import net.springfieldusa.security.SecurityException;
import net.springfieldusa.security.SecurityService;

@Component(service = TokenService.class)
public class TokenComponent extends AbstractComponent implements TokenService
{
  public @interface Config
  {
    long tokenExpirationAmount() default 1;
    String tokenExpirationUnit() default "DAYS";
  }
 
  private long tokenExpirationAmount;
  private ChronoUnit tokenExpirationUnit;

  private volatile SecurityService securityService;
  private volatile EncryptionSecretProvider secretProvider;
  private volatile Set<ClaimsProvider> claimsProviders = new CopyOnWriteArraySet<>();
  private Key key;

  @Activate
  public void activate(Config config)
  {
    tokenExpirationAmount = config.tokenExpirationAmount();
    tokenExpirationUnit = ChronoUnit.valueOf(config.tokenExpirationUnit());

    byte[] keyData = new byte[64];
    System.arraycopy(secretProvider.getSecret().getBytes(), 0, keyData, 0, secretProvider.getSecret().getBytes().length);
    key = new SecretKeySpec(keyData, SignatureAlgorithm.HS512.getJcaName());
  }

  @Override
  public String createToken(ContainerRequestContext context, UnencryptedCredential credentials) throws TokenException
  {
    if(credentials == null)
      return null;
    
    try
    {
      Principal principal = securityService.authenticate(credentials);

      if (principal == null)
        return null;

      Map<String, Object> claims = new HashMap<>();
      claims.put("userId", principal.getName());
      claims.put("exp", Instant.now().plus(tokenExpirationAmount, tokenExpirationUnit).getEpochSecond());
      
      for(ClaimsProvider claimsProvider : claimsProviders)
        claimsProvider.addClaims(claims, context, principal);
      
      return Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, key).compact();
    }
    catch (SecurityException e)
    {
      log(LogService.LOG_DEBUG, "Failed to create JWT token", e);
      throw new TokenException(e);
    }
  }

  @Override
  public Map<String, Object> verifyToken(String token) throws TokenException
  {
    // TODO : look at adding a token cache
    
    try
    {
      return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
    }
    catch (ExpiredJwtException | io.jsonwebtoken.SignatureException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e)
    {
      log(LogService.LOG_DEBUG, "JWT token verification exception", e);
      throw new TokenException(e);
    }
  }

  @Reference(unbind = "-")
  public void bindSecurityService(SecurityService securityService)
  {
    this.securityService = securityService;
  }

  @Reference(unbind = "-")
  public void bindEncryptionSecretProvider(EncryptionSecretProvider secretProvider)
  {
    this.secretProvider = secretProvider;
  }
  
  @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
  public void bindClaimsProvider(ClaimsProvider claimsProvider)
  {
    claimsProviders.add(claimsProvider);
  }
  
  public void unbindClaimsProvider(ClaimsProvider claimsProvider)
  {
    claimsProviders.remove(claimsProvider);
  }
}
