package net.springfieldusa.data.comp.junit.tests;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import net.springfieldusa.data.ApplicationException;
import net.springfieldusa.data.DataProcessor;
import net.springfieldusa.data.EntitySecurityProvider;
import net.springfieldusa.data.comp.UnsecureApplicationDataComponent;
import net.springfieldusa.entity.EntityObject;
import net.springfieldusa.storage.DuplicateIdException;
import net.springfieldusa.storage.StorageService;

public class TestUnsecureApplicationDataComponent
{
  private static final String COLLECTION = "junit";
  private UnsecureApplicationDataComponent unsecureApplicationDataComponent;
  private EntitySecurityProvider entitySecurityProvider;
  private StorageService storageService;
  private DataProcessor dataProcessor;
  private Principal principal;
  private EntityObject dataObject;
  private Map<String, Object> dataProcessorProperties;
  
  @Before
  public void setUp()
  {
    unsecureApplicationDataComponent = new UnsecureApplicationDataComponent();
    
    entitySecurityProvider = mock(EntitySecurityProvider.class);
    storageService = mock(StorageService.class);
    dataProcessor = mock(DataProcessor.class);
    principal = mock(Principal.class);
    dataObject = new EntityObject();
    
    dataProcessorProperties = new HashMap<>();
    dataProcessorProperties.put("collection", COLLECTION);
    
    unsecureApplicationDataComponent.bindEntitySecurityProvider(entitySecurityProvider);
    unsecureApplicationDataComponent.bindStorageSerivce(storageService);
  }
  
  @Test
  public void testCreateWithoutDataProcessor() throws DuplicateIdException, ApplicationException
  {
    when(storageService.create(COLLECTION, dataObject)).thenReturn(dataObject);

    assertThat(unsecureApplicationDataComponent.create(principal, COLLECTION, dataObject), is(sameInstance(dataObject)));
    verify(storageService).create(COLLECTION, dataObject);
    verify(entitySecurityProvider).createObjectSecurity(dataObject, principal);
    verifyZeroInteractions(dataProcessor);
  }

  @Test
  public void testCreateWithDataProcessor() throws DuplicateIdException, ApplicationException
  {
    unsecureApplicationDataComponent.bindDataProcessor(dataProcessor, dataProcessorProperties);

    when(storageService.create(COLLECTION, dataObject)).thenReturn(dataObject);
    when(dataProcessor.handleCreate(principal, dataObject)).thenReturn(dataObject);
    
    assertThat(unsecureApplicationDataComponent.create(principal, COLLECTION, dataObject), is(sameInstance(dataObject)));
    verify(dataProcessor).handleCreate(principal, dataObject);
    verify(storageService).create(COLLECTION, dataObject);
    verify(entitySecurityProvider).createObjectSecurity(dataObject, principal);
  }
  
  @Test
  @Ignore
  @SuppressWarnings("unchecked")
  public void testRetrieveByIdWithoutDataProcessor() throws DuplicateIdException, ApplicationException
  {
    // FIXME : any(Supplier.class) is not matching against EntityObject::new
    when(storageService.retrieve(eq(COLLECTION), eq("id"), any(Supplier.class))).thenReturn(dataObject);
    
    assertThat(unsecureApplicationDataComponent.retrieve(principal, COLLECTION, "id"), is(sameInstance(dataObject)));
    verify(storageService).retrieve(COLLECTION, "id");
    verifyZeroInteractions(dataProcessor);    
    verifyZeroInteractions(entitySecurityProvider);
  }
}
