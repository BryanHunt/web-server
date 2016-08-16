package net.springfieldusa.data.comp.junit.tests;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.springfieldusa.data.ApplicationException;
import net.springfieldusa.data.comp.EntitySecurityProviderComponent;
import net.springfieldusa.data.comp.EntitySecurityProviderComponent.Config;
import net.springfieldusa.entity.EntityObject;
import net.springfieldusa.entity.ObjectSecurity;
import net.springfieldusa.security.SecurityException;
import net.springfieldusa.security.SecurityService;

public class TestEntitySecurityProviderComponent
{
  private EntitySecurityProviderComponent entitySecurityProviderComponent;
  private SecurityService securityService;
  private Map<String, Object> properties;
  private Principal principal;
  private EntityObject dataObject;
  
  @Before
  @SuppressWarnings("restriction")
  public void setUp() throws Exception
  {
    entitySecurityProviderComponent = new EntitySecurityProviderComponent();
    properties = new HashMap<>();
    dataObject = new EntityObject();

    securityService = mock(SecurityService.class);
    principal = mock(Principal.class);
    
    entitySecurityProviderComponent.bindSecurityService(securityService);
    entitySecurityProviderComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
  }
  
  @Test
  public void testSetObjectSecurity() throws ApplicationException
  {
    when(principal.getName()).thenReturn("junit");
    entitySecurityProviderComponent.setObjectSecurity(dataObject, principal);
    
    assertThat(dataObject.getSecurity(), is(notNullValue()));
    assertTrue(dataObject.getSecurity().isOwner(principal.getName()));
  }

  @Test
  public void testSetObjectSecurityUserProvidedSecurity() throws ApplicationException
  {
    ObjectSecurity security = new ObjectSecurity();
    security.setOwner("test");
    dataObject.setSecurity(security);
    
    when(principal.getName()).thenReturn("junit");
    entitySecurityProviderComponent.setObjectSecurity(dataObject, principal);
    
    assertThat(dataObject.getSecurity(), is(notNullValue()));
    assertTrue(dataObject.getSecurity().isOwner(principal.getName()));
  }

  @Test
  public void testSetObjectSecurityAdminProvidedSecurity() throws ApplicationException, SecurityException
  {
    ObjectSecurity security = new ObjectSecurity();
    security.setOwner("test");
    dataObject.setSecurity(security);
    
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(true);
    when(principal.getName()).thenReturn("junit");
    entitySecurityProviderComponent.setObjectSecurity(dataObject, principal);
    
    assertThat(dataObject.getSecurity(), is(notNullValue()));
    assertTrue(dataObject.getSecurity().isOwner("test"));
  }
  
  @Test(expected = ApplicationException.class)
  public void testSetObjectSecurityWithSecurityException() throws SecurityException, ApplicationException
  {
    ObjectSecurity security = new ObjectSecurity();
    security.setOwner("test");
    dataObject.setSecurity(security);
    
    when(securityService.authorizeForRole(principal, "admin")).thenThrow(new SecurityException());
    when(principal.getName()).thenReturn("junit");
    entitySecurityProviderComponent.setObjectSecurity(dataObject, principal);
  }
}
