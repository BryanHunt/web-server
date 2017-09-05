package net.springfieldusa.jwt.jjwt.comp.junit.tests;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.springfieldusa.jwt.EncryptionSecretProvider;
import net.springfieldusa.jwt.TokenException;
import net.springfieldusa.jwt.jjwt.comp.TokenComponent;
import net.springfieldusa.jwt.jjwt.comp.TokenComponent.Config;

public class TestTokenComponent
{
  private TokenComponent tokenComponent;
  private EncryptionSecretProvider secretProvider;
  private Principal principal;
  
  @SuppressWarnings("restriction")
  @Before
  public void setUp() throws Exception
  {
    tokenComponent = new TokenComponent();
    
    secretProvider = mock(EncryptionSecretProvider.class);
    principal = mock(Principal.class);

    when(secretProvider.getSecret()).thenReturn("secret");

    tokenComponent.bindEncryptionSecretProvider(secretProvider);
    
    tokenComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, Collections.emptyMap()));
  }
  
  @Test
  public void testCreateToken() throws TokenException, SecurityException
  {
    when(principal.getName()).thenReturn("junit");
    
    assertThat(tokenComponent.createToken(principal, null), is(notNullValue()));
  }

  @Test
  public void testVerifyToken() throws TokenException, SecurityException
  {
    when(principal.getName()).thenReturn("junit");
    
    String token = tokenComponent.createToken(principal, null);
    Map<String, Object> attributes = tokenComponent.verifyToken(token);
    
    assertThat(attributes, hasEntry("userId", "junit"));
  }

  @Test(expected = TokenException.class)
  public void testVerifyMalformedToken() throws TokenException, SecurityException
  {
    tokenComponent.verifyToken("jibberish");
  }
}
