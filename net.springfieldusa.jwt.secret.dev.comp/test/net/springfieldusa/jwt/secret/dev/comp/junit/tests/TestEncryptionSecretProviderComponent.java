package net.springfieldusa.jwt.secret.dev.comp.junit.tests;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;

import org.junit.Before;
import org.junit.Test;

import net.springfieldusa.jwt.secret.dev.comp.EncryptionSecretProviderComponent;

public class TestEncryptionSecretProviderComponent
{
  private EncryptionSecretProviderComponent encryptionSecretProviderComponent;
  
  @Before
  public void setUp()
  {
    encryptionSecretProviderComponent = new EncryptionSecretProviderComponent();
  }
  
  @Test
  public void testGetSecret()
  {
    assertThat(encryptionSecretProviderComponent.getSecret(), not(isEmptyOrNullString()));
  }
}
