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

package net.springfieldusa.jwt.auth0.comp;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SignatureException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;

import net.springfieldusa.comp.AbstractComponent;
import net.springfieldusa.credentials.UnencryptedCredential;
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
  private JWTSigner signer;
  private JWTVerifier verifier;

  @Activate
  public void activate()
  {
    signer = new JWTSigner(secretProvider.getSecret());
    verifier = new JWTVerifier(secretProvider.getSecret());
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

      Collection<String> roles = securityService.getRoles(principal);

      Map<String, Object> claims = new HashMap<>();
      claims.put("userId", principal.getName());
      claims.put("roles", roles);

      if (principal.getName().equals("admin"))
        claims.put("isAdmin", true);

      return signer.sign(claims);
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
    try
    {
      return verifier.verify(token);
    }
    catch (InvalidKeyException | NoSuchAlgorithmException | IllegalStateException | SignatureException | IOException | JWTVerifyException e)
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
}
