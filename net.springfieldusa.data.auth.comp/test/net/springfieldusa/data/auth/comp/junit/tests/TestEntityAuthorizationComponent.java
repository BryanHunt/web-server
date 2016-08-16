package net.springfieldusa.data.auth.comp.junit.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import net.springfieldusa.data.auth.comp.EntityAuthorizationComponent;
import net.springfieldusa.data.auth.comp.EntityAuthorizationComponent.Config;
import net.springfieldusa.entity.EntityObject;
import net.springfieldusa.entity.ObjectSecurity;
import net.springfieldusa.security.SecurityException;
import net.springfieldusa.security.SecurityService;

public class TestEntityAuthorizationComponent
{
  private EntityAuthorizationComponent entityAuthorizationComponent;
  private SecurityService securityService;
  private Map<String, Object> properties;
  private Principal principal;
  private EntityObject dataObject;
  private Collection<EntityObject> dataObjects;
  
  @Before
  public void setUp()
  {
    entityAuthorizationComponent = new EntityAuthorizationComponent();
    properties = new HashMap<>();
    
    dataObject = new EntityObject();
    dataObjects = new ArrayList<>();
    dataObjects.add(dataObject);
    
    securityService = mock(SecurityService.class);
    principal = mock(Principal.class);
    
    entityAuthorizationComponent.bindSecurityService(securityService);
  }
  
  //--- Test create object ------------------------------------------------------------------------
  
  @Test
  @SuppressWarnings("restriction")
  public void testIsCreateAuthorizedForValidPrincipal() throws Exception
  {
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertTrue(entityAuthorizationComponent.isCreateAuthorizedFor(principal, "collection", dataObject));
  }

  @Test
  @SuppressWarnings("restriction")
  public void testIsCreateAuthorizedForInvalidPrincipal() throws Exception
  {
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertFalse(entityAuthorizationComponent.isCreateAuthorizedFor(null, "collection", dataObject));
  }

  //--- Test retrieve single object ---------------------------------------------------------------
  
  @Test
  @SuppressWarnings("restriction")
  public void testIsRetrieveAuthorizedForInvalidPrincipalSingleObject() throws Exception
  {
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertFalse(entityAuthorizationComponent.isRetrieveAuthorizedFor(null, "collection", dataObject));
  }

  @Test
  @SuppressWarnings("restriction")
  public void testIsRetrieveAuthorizedForValidPrincipalAdminSingleObject() throws Exception
  {
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(true);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertTrue(entityAuthorizationComponent.isRetrieveAuthorizedFor(principal, "collection", dataObject));
  }

  @Test
  @SuppressWarnings("restriction")
  public void testIsRetrieveAuthorizedForValidPrincipalNotMisingObjectSecuritySingleObject() throws Exception
  {
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);

    properties.put("missingSecurityAuthorization", true);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertTrue(entityAuthorizationComponent.isRetrieveAuthorizedFor(principal, "collection", dataObject));
  }

  @Test
  @SuppressWarnings("restriction")
  public void testIsRetrieveAuthorizedForValidPrincipalMisingObjectSecuritySingleObject() throws Exception
  {
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);

    properties.put("missingSecurityAuthorization", false);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertFalse(entityAuthorizationComponent.isRetrieveAuthorizedFor(principal, "collection", dataObject));
  }

  @Test
  @SuppressWarnings("restriction")
  public void testIsRetrieveAuthorizedForValidPrincipalIsOwnerSingleObject() throws Exception
  {
    when(principal.getName()).thenReturn("junit");
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);

    ObjectSecurity security = new ObjectSecurity();
    security.put(ObjectSecurity.KEY_OWNER, "junit");
    dataObject.setSecurity(security);
    
    properties.put("missingSecurityAuthorization", false);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertTrue(entityAuthorizationComponent.isRetrieveAuthorizedFor(principal, "collection", dataObject));
  }
  
  @Test
  @SuppressWarnings("restriction")
  public void testIsRetrieveAuthorizedForValidPrincipalIsNotOwnerSingleObject() throws Exception
  {
    when(principal.getName()).thenReturn("test");
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);

    ObjectSecurity security = new ObjectSecurity();
    security.put(ObjectSecurity.KEY_OWNER, "junit");
    dataObject.setSecurity(security);
    
    properties.put("missingSecurityAuthorization", false);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertFalse(entityAuthorizationComponent.isRetrieveAuthorizedFor(principal, "collection", dataObject));
  }

  @Test
  @SuppressWarnings("restriction")
  public void testIsRetrieveAuthorizedForValidPrincipalInReadGroupSingleObject() throws Exception
  {
    Set<String> groups = new HashSet<>();
    groups.add("readGroup");

    when(principal.getName()).thenReturn("test");
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);
    when(securityService.getRoles(principal)).thenReturn(groups);
    
    ObjectSecurity security = new ObjectSecurity();
    security.put(ObjectSecurity.KEY_OWNER, "junit");
    security.put(ObjectSecurity.KEY_READ_GROUPS, groups);
    dataObject.setSecurity(security);
    
    properties.put("missingSecurityAuthorization", false);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertTrue(entityAuthorizationComponent.isRetrieveAuthorizedFor(principal, "collection", dataObject));
  }
  
  @Test
  @SuppressWarnings({ "restriction" })
  public void testIsRetrieveAuthorizedForWithSecurityExceptionSingleObject() throws Exception
  {
    when(securityService.authorizeForRole(principal, "admin")).thenThrow(new SecurityException());
    
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertFalse(entityAuthorizationComponent.isRetrieveAuthorizedFor(principal, "collection", dataObject));
  }
  
  //--- Test retrieve multiple objects ------------------------------------------------------------
  
  @Test
  @SuppressWarnings("restriction")
  public void testIsRetrieveAuthorizedForInvalidPrincipalMultipleObjects() throws Exception
  {
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertFalse(entityAuthorizationComponent.isRetrieveAuthorizedFor(null, "collection", dataObjects));
  }

  @Test
  @SuppressWarnings("restriction")
  public void testIsRetrieveAuthorizedForValidPrincipalAdminMultipleObjects() throws Exception
  {
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(true);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertTrue(entityAuthorizationComponent.isRetrieveAuthorizedFor(principal, "collection", dataObjects));
  }

  @Test
  @SuppressWarnings("restriction")
  public void testIsRetrieveAuthorizedForValidPrincipalNotMisingObjectSecurityMultipleObjects() throws Exception
  {
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);

    properties.put("missingSecurityAuthorization", true);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertTrue(entityAuthorizationComponent.isRetrieveAuthorizedFor(principal, "collection", dataObjects));
  }

  @Test
  @SuppressWarnings("restriction")
  public void testIsRetrieveAuthorizedForValidPrincipalMisingObjectSecurityMultipleObjects() throws Exception
  {
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);

    properties.put("missingSecurityAuthorization", false);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertFalse(entityAuthorizationComponent.isRetrieveAuthorizedFor(principal, "collection", dataObjects));
  }

  @Test
  @SuppressWarnings("restriction")
  public void testIsRetrieveAuthorizedForValidPrincipalIsOwnerMultipleObjects() throws Exception
  {
    when(principal.getName()).thenReturn("junit");
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);

    ObjectSecurity security = new ObjectSecurity();
    security.put(ObjectSecurity.KEY_OWNER, "junit");
    dataObject.setSecurity(security);
    
    properties.put("missingSecurityAuthorization", false);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertTrue(entityAuthorizationComponent.isRetrieveAuthorizedFor(principal, "collection", dataObjects));
  }
  
  @Test
  @SuppressWarnings("restriction")
  public void testIsRetrieveAuthorizedForValidPrincipalIsNotOwnerMultipleObjects() throws Exception
  {
    when(principal.getName()).thenReturn("test");
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);

    ObjectSecurity security = new ObjectSecurity();
    security.put(ObjectSecurity.KEY_OWNER, "junit");
    dataObject.setSecurity(security);
    
    properties.put("missingSecurityAuthorization", false);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertFalse(entityAuthorizationComponent.isRetrieveAuthorizedFor(principal, "collection", dataObjects));
  }

  @Test
  @SuppressWarnings("restriction")
  public void testIsRetrieveAuthorizedForValidPrincipalInReadGroupMultipleObjects() throws Exception
  {
    Set<String> groups = new HashSet<>();
    groups.add("readGroup");

    when(principal.getName()).thenReturn("test");
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);
    when(securityService.getRoles(principal)).thenReturn(groups);
    
    ObjectSecurity security = new ObjectSecurity();
    security.put(ObjectSecurity.KEY_OWNER, "junit");
    security.put(ObjectSecurity.KEY_READ_GROUPS, groups);
    dataObject.setSecurity(security);
    
    properties.put("missingSecurityAuthorization", false);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertTrue(entityAuthorizationComponent.isRetrieveAuthorizedFor(principal, "collection", dataObjects));
  }
  
  @Test
  @SuppressWarnings({ "restriction" })
  public void testIsRetrieveAuthorizedForWithSecurityExceptionMultipleObjects() throws Exception
  {
    when(securityService.authorizeForRole(principal, "admin")).thenThrow(new SecurityException());
    
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertFalse(entityAuthorizationComponent.isRetrieveAuthorizedFor(principal, "collection", dataObjects));
  }
  
  //--- Test update object ------------------------------------------------------------------------
  
  @Test
  @SuppressWarnings("restriction")
  public void testIsUpdateAuthorizedForInvalidPrincipal() throws Exception
  {
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertFalse(entityAuthorizationComponent.isUpdateAuthorizedFor(null, "collection", dataObject));
  }

  @Test
  @SuppressWarnings("restriction")
  public void testIsUpdateAuthorizedForValidPrincipalAdmin() throws Exception
  {
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(true);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertTrue(entityAuthorizationComponent.isUpdateAuthorizedFor(principal, "collection", dataObject));
  }

  @Test
  @SuppressWarnings("restriction")
  public void testIsUpdateAuthorizedForValidPrincipalNotMisingObjectSecurity() throws Exception
  {
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);

    properties.put("missingSecurityAuthorization", true);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertTrue(entityAuthorizationComponent.isUpdateAuthorizedFor(principal, "collection", dataObject));
  }

  @Test
  @SuppressWarnings("restriction")
  public void testIsUpdateAuthorizedForValidPrincipalMisingObjectSecurity() throws Exception
  {
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);

    properties.put("missingSecurityAuthorization", false);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertFalse(entityAuthorizationComponent.isUpdateAuthorizedFor(principal, "collection", dataObject));
  }
  
  @Test
  @SuppressWarnings("restriction")
  public void testIsUpdateAuthorizedForValidPrincipalIsOwner() throws Exception
  {
    when(principal.getName()).thenReturn("junit");
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);

    ObjectSecurity security = new ObjectSecurity();
    security.put(ObjectSecurity.KEY_OWNER, "junit");
    dataObject.setSecurity(security);
    
    properties.put("missingSecurityAuthorization", false);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertTrue(entityAuthorizationComponent.isUpdateAuthorizedFor(principal, "collection", dataObject));
  }
  
  @Test
  @SuppressWarnings("restriction")
  public void testIsUpdateAuthorizedForValidPrincipalIsNotOwner() throws Exception
  {
    when(principal.getName()).thenReturn("test");
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);

    ObjectSecurity security = new ObjectSecurity();
    security.put(ObjectSecurity.KEY_OWNER, "junit");
    dataObject.setSecurity(security);
    
    properties.put("missingSecurityAuthorization", false);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertFalse(entityAuthorizationComponent.isUpdateAuthorizedFor(principal, "collection", dataObject));
  }

  @Test
  @SuppressWarnings("restriction")
  public void testIsUpdateAuthorizedForValidPrincipalInWriteGroup() throws Exception
  {
    Set<String> groups = new HashSet<>();
    groups.add("writeGroup");

    when(principal.getName()).thenReturn("test");
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);
    when(securityService.getRoles(principal)).thenReturn(groups);
    
    ObjectSecurity security = new ObjectSecurity();
    security.put(ObjectSecurity.KEY_OWNER, "junit");
    security.put(ObjectSecurity.KEY_WRITE_GROUPS, groups);
    dataObject.setSecurity(security);
    
    properties.put("missingSecurityAuthorization", false);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertTrue(entityAuthorizationComponent.isUpdateAuthorizedFor(principal, "collection", dataObject));
  }
  
  @Test
  @SuppressWarnings({ "restriction" })
  public void testIsUpdateAuthorizedForWithSecurityException() throws Exception
  {
    when(securityService.authorizeForRole(principal, "admin")).thenThrow(new SecurityException());
    
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertFalse(entityAuthorizationComponent.isUpdateAuthorizedFor(principal, "collection", dataObject));
  }
  
  //--- Test delete object ------------------------------------------------------------------------
  
  @Test
  @SuppressWarnings("restriction")
  public void testIsDeleteAuthorizedForInvalidPrincipal() throws Exception
  {
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertFalse(entityAuthorizationComponent.isDeleteAuthorizedFor(null, "collection", dataObject));
  }

  @Test
  @SuppressWarnings("restriction")
  public void testIsDeleteAuthorizedForValidPrincipalAdmin() throws Exception
  {
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(true);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertTrue(entityAuthorizationComponent.isDeleteAuthorizedFor(principal, "collection", dataObject));
  }

  @Test
  @SuppressWarnings("restriction")
  public void testIsDeleteAuthorizedForValidPrincipalNotMisingObjectSecurity() throws Exception
  {
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);

    properties.put("missingSecurityAuthorization", true);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertTrue(entityAuthorizationComponent.isDeleteAuthorizedFor(principal, "collection", dataObject));
  }

  @Test
  @SuppressWarnings("restriction")
  public void testIsDeleteAuthorizedForValidPrincipalMisingObjectSecurity() throws Exception
  {
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);

    properties.put("missingSecurityAuthorization", false);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertFalse(entityAuthorizationComponent.isDeleteAuthorizedFor(principal, "collection", dataObject));
  }
  
  @Test
  @SuppressWarnings("restriction")
  public void testIsDeleteAuthorizedForValidPrincipalIsOwner() throws Exception
  {
    when(principal.getName()).thenReturn("junit");
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);

    ObjectSecurity security = new ObjectSecurity();
    security.put(ObjectSecurity.KEY_OWNER, "junit");
    dataObject.setSecurity(security);
    
    properties.put("missingSecurityAuthorization", false);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertTrue(entityAuthorizationComponent.isDeleteAuthorizedFor(principal, "collection", dataObject));
  }
  
  @Test
  @SuppressWarnings("restriction")
  public void testIsDeleteAuthorizedForValidPrincipalIsNotOwner() throws Exception
  {
    when(principal.getName()).thenReturn("test");
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);

    ObjectSecurity security = new ObjectSecurity();
    security.put(ObjectSecurity.KEY_OWNER, "junit");
    dataObject.setSecurity(security);
    
    properties.put("missingSecurityAuthorization", false);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertFalse(entityAuthorizationComponent.isDeleteAuthorizedFor(principal, "collection", dataObject));
  }

  @Test
  @SuppressWarnings("restriction")
  public void testIsDeleteAuthorizedForValidPrincipalInDeleteGroup() throws Exception
  {
    Set<String> groups = new HashSet<>();
    groups.add("deleteGroup");

    when(principal.getName()).thenReturn("test");
    when(securityService.authorizeForRole(principal, "admin")).thenReturn(false);
    when(securityService.getRoles(principal)).thenReturn(groups);
    
    ObjectSecurity security = new ObjectSecurity();
    security.put(ObjectSecurity.KEY_OWNER, "junit");
    security.put(ObjectSecurity.KEY_DELETE_GROUPS, groups);
    dataObject.setSecurity(security);
    
    properties.put("missingSecurityAuthorization", false);
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertTrue(entityAuthorizationComponent.isDeleteAuthorizedFor(principal, "collection", dataObject));
  }
  
  @Test
  @SuppressWarnings({ "restriction" })
  public void testIsDeleteAuthorizedForWithSecurityException() throws Exception
  {
    when(securityService.authorizeForRole(principal, "admin")).thenThrow(new SecurityException());
    
    entityAuthorizationComponent.activate(aQute.lib.converter.Converter.cnv(Config.class, properties));
    
    assertFalse(entityAuthorizationComponent.isDeleteAuthorizedFor(principal, "collection", dataObject));
  }
}
