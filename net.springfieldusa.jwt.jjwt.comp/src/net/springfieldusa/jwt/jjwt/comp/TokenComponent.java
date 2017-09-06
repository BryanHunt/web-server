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

import javax.crypto.spec.SecretKeySpec;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import net.springfieldusa.comp.AbstractComponent;
import net.springfieldusa.jwt.EncryptionSecretProvider;
import net.springfieldusa.jwt.TokenException;
import net.springfieldusa.jwt.TokenExpiredException;
import net.springfieldusa.jwt.TokenService;

@Component
public class TokenComponent extends AbstractComponent implements TokenService
{
  public @interface Config
  {
    long tokenExpirationAmount() default 1;

    String tokenExpirationUnit() default "DAYS";
  }

  private long tokenExpirationAmount;
  private ChronoUnit tokenExpirationUnit;

  private volatile EncryptionSecretProvider secretProvider;
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
  public String createToken(Principal principal, Map<String, Object> claims) throws TokenException
  {
    if(claims == null)
      claims = new HashMap<>();
    
    claims.put("userId", principal.getName());
    claims.put("exp", Instant.now().plus(tokenExpirationAmount, tokenExpirationUnit).getEpochSecond());  
    
    return Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, key).compact();
  }

  @Override
  public Map<String, Object> verifyToken(String token) throws TokenException
  {
    // TODO : look at adding a token cache

    try
    {
      return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
    }
    catch (ExpiredJwtException e)
    {
      log(LogService.LOG_DEBUG, "JWT token expired exception", e);
      throw new TokenExpiredException(e);
    }
    catch (io.jsonwebtoken.SignatureException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e)
    {
      log(LogService.LOG_DEBUG, "JWT token exception", e);
      throw new TokenException(e);
    }
  }

  @Reference(unbind = "-")
  public void bindEncryptionSecretProvider(EncryptionSecretProvider secretProvider)
  {
    this.secretProvider = secretProvider;
  }
}
