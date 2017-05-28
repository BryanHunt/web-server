package net.springfieldusa.data.auth.comp.junit.tests;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.springfieldusa.data.ApplicationException;
import net.springfieldusa.data.auth.comp.EntitySecurityProviderComponent;
import net.springfieldusa.data.auth.comp.EntitySecurityProviderComponent.Config;
import net.springfieldusa.data.auth.comp.ObjectSecurity;
import net.springfieldusa.entity.EntityObject;
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
    
    assertTrue(entitySecurityProviderComponent.getObjectSecurity(dataObject).isOwner(principal.getName()));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSetObjectSecurityUserProvidedSecurity() throws ApplicationException, SecurityException
  {
    when(principal.getName()).thenReturn("test");
    entitySecurityProviderComponent.setObjectSecurity(dataObject, principal);
    
    ObjectSecurity security = new ObjectSecurity((Map<String, Object>) dataObject.getMeta().get("security"));
    security.setOwner("junit");
    
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);
    entitySecurityProviderComponent.setObjectSecurity(dataObject, principal);
    
    assertTrue(entitySecurityProviderComponent.getObjectSecurity(dataObject).isOwner("test"));
  }

  @Test
  public void testSetObjectSecurityAdminProvidedSecurity() throws ApplicationException, SecurityException
  {
    when(principal.getName()).thenReturn("test");
    entitySecurityProviderComponent.setObjectSecurity(dataObject, principal);
    
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(true);
    when(principal.getName()).thenReturn("junit");
    entitySecurityProviderComponent.setObjectSecurity(dataObject, principal);
    
    assertTrue(entitySecurityProviderComponent.getObjectSecurity(dataObject).isOwner("test"));
  }
  
  @Test(expected = ApplicationException.class)
  public void testSetObjectSecurityWithSecurityException() throws SecurityException, ApplicationException
  {
    entitySecurityProviderComponent.setObjectSecurity(dataObject, principal);
    
    when(securityService.authorizeForRole(principal, "admin")).thenThrow(new SecurityException());
    when(principal.getName()).thenReturn("junit");
    entitySecurityProviderComponent.setObjectSecurity(dataObject, principal);
  }
}
