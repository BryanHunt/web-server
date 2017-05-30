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
import net.springfieldusa.entity.EntityObject;

public class TestEntitySecurityProviderComponent
{
  private EntitySecurityProviderComponent entitySecurityProviderComponent;
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

    principal = mock(Principal.class);
    
    entitySecurityProviderComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
  }
  
  @Test
  public void testSetObjectSecurity() throws ApplicationException
  {
    when(principal.getName()).thenReturn("junit");
    entitySecurityProviderComponent.createObjectSecurity(dataObject, principal);
    
    assertTrue(entitySecurityProviderComponent.getObjectSecurity(dataObject).isOwner(principal.getName()));
  }
}
