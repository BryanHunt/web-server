package net.springfieldusa.jwt.jjwt.comp.junit.tests;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.springfieldusa.credentials.UnencryptedCredential;
import net.springfieldusa.jwt.ClaimsProvider;
import net.springfieldusa.jwt.EncryptionSecretProvider;
import net.springfieldusa.jwt.TokenException;
import net.springfieldusa.jwt.jjwt.comp.TokenComponent;
import net.springfieldusa.security.SecurityException;
import net.springfieldusa.security.SecurityService;

public class TestTokenComponent
{
  private TokenComponent tokenComponent;
  private SecurityService securityService;
  private EncryptionSecretProvider secretProvider;
  private Principal principal;
  private ClaimsProvider claimsProvider;
  
  @Before
  public void setUp()
  {
    tokenComponent = new TokenComponent();
    
    securityService = mock(SecurityService.class);
    secretProvider = mock(EncryptionSecretProvider.class);
    claimsProvider = mock(ClaimsProvider.class);
    principal = mock(Principal.class);

    when(secretProvider.getSecret()).thenReturn("secret");

    tokenComponent.bindSecurityService(securityService);
    tokenComponent.bindEncryptionSecretProvider(secretProvider);
    tokenComponent.bindClaimsProvider(claimsProvider);
    tokenComponent.activate();
  }
  
  @Test
  public void testCreateTokenWithValidCredentials() throws TokenException, SecurityException
  {
    UnencryptedCredential credentials = new UnencryptedCredential("junit", "pass");

    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", "junit");
    
    when(claimsProvider.getClaims(principal)).thenReturn(claims);
    when(securityService.authenticate(credentials)).thenReturn(principal);
    when(principal.getName()).thenReturn("junit");
    
    assertThat(tokenComponent.createToken(credentials), is(notNullValue()));
  }

  @Test
  public void testCreateTokenWithInvalidCredentials() throws TokenException, SecurityException
  {
    UnencryptedCredential credentials = new UnencryptedCredential("junit", "pass");

    when(securityService.authenticate(credentials)).thenReturn(null);
    
    assertThat(tokenComponent.createToken(credentials), is(nullValue()));
  }

  @Test(expected = TokenException.class)
  public void testCreateTokenWithSecurityException() throws TokenException, SecurityException
  {
    UnencryptedCredential credentials = new UnencryptedCredential("junit", "pass");

    when(securityService.authenticate(credentials)).thenThrow(new SecurityException());
    
    tokenComponent.createToken(credentials);
  }

  @Test
  public void testVerifyToken() throws TokenException, SecurityException
  {
    UnencryptedCredential credentials = new UnencryptedCredential("junit", "pass");

    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", "junit");
    
    when(claimsProvider.getClaims(principal)).thenReturn(claims);
    when(securityService.authenticate(credentials)).thenReturn(principal);
    when(principal.getName()).thenReturn("junit");
    
    String token = tokenComponent.createToken(credentials);
    Map<String, Object> attributes = tokenComponent.verifyToken(token);
    
    assertThat(attributes, hasEntry("userId", "junit"));
  }

  @Test(expected = TokenException.class)
  public void testVerifyMalformedToken() throws TokenException, SecurityException
  {
    tokenComponent.verifyToken("jibberish");
  }
}
