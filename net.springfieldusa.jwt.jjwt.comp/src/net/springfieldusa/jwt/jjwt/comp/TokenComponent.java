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
  private volatile SecurityService securityService;
  private volatile EncryptionSecretProvider secretProvider;
  private volatile ClaimsProvider claimsProvider;
  private Key key;

  @Activate
  public void activate()
  {
    byte[] keyData = new byte[64];
    System.arraycopy(secretProvider.getSecret().getBytes(), 0, keyData, 0, secretProvider.getSecret().getBytes().length);
    key = new SecretKeySpec(keyData, SignatureAlgorithm.HS512.getJcaName());
  }

  @Override
  public String createToken(UnencryptedCredential credentials) throws TokenException
  {
    if(credentials == null)
      return null;
    
    try
    {
      Principal principal = securityService.authenticate(credentials);

      if (principal == null)
        return null;

      return Jwts.builder().setClaims(claimsProvider.getClaims(principal)).signWith(SignatureAlgorithm.HS512, key).compact();
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
  
  @Reference(unbind = "-")
  public void bindClaimsProvider(ClaimsProvider claimsProvider)
  {
    this.claimsProvider = claimsProvider;
  }
}
