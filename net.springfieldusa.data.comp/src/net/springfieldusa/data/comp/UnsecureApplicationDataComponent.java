package net.springfieldusa.data.comp;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import net.springfieldusa.comp.AbstractComponent;
import net.springfieldusa.data.ApplicationDataService;
import net.springfieldusa.data.ApplicationException;
import net.springfieldusa.data.DataProcessor;
import net.springfieldusa.data.EntitySecurityProvider;
import net.springfieldusa.entity.EntityObject;
import net.springfieldusa.storage.DuplicateIdException;
import net.springfieldusa.storage.StorageService;

@Component(service = ApplicationDataService.class, property = {"secure:Boolean=false"})
public class UnsecureApplicationDataComponent extends AbstractComponent implements ApplicationDataService
{
  private volatile EntitySecurityProvider securityProvider;
  private volatile StorageService storageService;
  private Map<String, DataProcessor> dataProcessors = new ConcurrentHashMap<>();
  
  @Override
  public <T extends EntityObject> T create(Principal principal, String collection, T data) throws DuplicateIdException, ApplicationException
  {
    securityProvider.createObjectSecurity(data, principal);

    DataProcessor dataProcessor = dataProcessors.get(collection);
    T processedData = dataProcessor != null ? dataProcessor.handleCreate(principal, data) : data;
    
    return storageService.create(collection, processedData);
  }

  @Override
  public EntityObject retrieve(Principal principal, String collection, String id) throws ApplicationException
  {
    return retrieve(principal, collection, id, EntityObject::new);
  }

  @Override
  public EntityObject retrieve(Principal principal, String collection, String key, String value) throws ApplicationException
  {
    return retrieve(principal, collection, key, value, EntityObject::new);
  }

  @Override
  public <T extends EntityObject> T retrieve(Principal principal, String collection, String id, Supplier<T> factory) throws ApplicationException
  {
    T data = storageService.retrieve(collection, id, factory);
    
    DataProcessor dataProcessor = dataProcessors.get(collection);
    
    if(dataProcessor != null)
      data = dataProcessor.handleRetrieve(principal, data);

    return data;
  }

  @Override
  public <T extends EntityObject> T retrieve(Principal principal, String collection, String key, String value, Supplier<T> factory) throws ApplicationException
  {
    T data = storageService.retrieve(collection, key, value, factory);
    
    DataProcessor dataProcessor = dataProcessors.get(collection);
    
    if(dataProcessor != null)
      data = dataProcessor.handleRetrieve(principal, data);

    return data;
  }

  @Override
  public Collection<EntityObject> find(Principal principal, String collection, Map<String, Object> query) throws ApplicationException
  {
    return find(principal, collection, query, EntityObject::new);
  }

  @Override
  public Collection<EntityObject> find(Principal principal, String collection, String query) throws ApplicationException
  {
    return find(principal, collection, query, EntityObject::new);
  }

  @Override
  public <T extends EntityObject> Collection<T> find(Principal principal, String collection, Map<String, Object> query, Supplier<T> factory) throws ApplicationException
  {
    Collection<T> data = storageService.find(collection, query, factory);
    
    DataProcessor dataProcessor = dataProcessors.get(collection);
    
    if(dataProcessor != null)
      data = dataProcessor.handleRetrieve(principal, data);

    return data;
  }

  @Override
  public <T extends EntityObject> Collection<T> find(Principal principal, String collection, String query, Supplier<T> factory) throws ApplicationException
  {
    Collection<T> data = storageService.find(collection, query, factory);
    
    DataProcessor dataProcessor = dataProcessors.get(collection);
    
    if(dataProcessor != null)
      data = dataProcessor.handleRetrieve(principal, data);

    return data;
  }

  @Override
  public Collection<EntityObject> find(Principal principal, String collection, Map<String, Object> query, int skip, int limit) throws ApplicationException
  {
    return find(principal, collection, query, skip, limit, EntityObject::new);
  }

  @Override
  public Collection<EntityObject> find(Principal principal, String collection, String query, int skip, int limit) throws ApplicationException
  {
    return find(principal, collection, query, skip, limit, EntityObject::new);
  }

  @Override
  public <T extends EntityObject> Collection<T> find(Principal principal, String collection, Map<String, Object> query, int skip, int limit, Supplier<T> factory) throws ApplicationException
  {
    Collection<T> data = storageService.find(collection, query, skip, limit, factory);
    
    DataProcessor dataProcessor = dataProcessors.get(collection);
    
    if(dataProcessor != null)
      data = dataProcessor.handleRetrieve(principal, data);

    return data;
  }

  @Override
  public <T extends EntityObject> Collection<T> find(Principal principal, String collection, String query, int skip, int limit, Supplier<T> factory) throws ApplicationException
  {
    Collection<T> data = storageService.find(collection, query, skip, limit, factory);
    
    DataProcessor dataProcessor = dataProcessors.get(collection);
    
    if(dataProcessor != null)
      data = dataProcessor.handleRetrieve(principal, data);

    return data;
  }

  @Override
  public <T extends EntityObject> long update(Principal principal, String collection, T data) throws ApplicationException
  {
    DataProcessor dataProcessor = dataProcessors.get(collection);
    T processedData = dataProcessor != null ? dataProcessor.handleUpdate(principal, data) : data;
    return storageService.update(collection, processedData);
  }

  @Override
  public <T extends EntityObject> long update(Principal principal, String collection, String query, T data) throws ApplicationException
  {
    DataProcessor dataProcessor = dataProcessors.get(collection);
    T processedData = dataProcessor != null ? dataProcessor.handleUpdate(principal, data) : data;
    return storageService.update(collection, query, processedData);
  }

  @Override
  public <T extends EntityObject> long patch(Principal principal, String collection, T data) throws ApplicationException
  {
    DataProcessor dataProcessor = dataProcessors.get(collection);
    T processedData = dataProcessor != null ? dataProcessor.handlePatch(principal, data) : data;
    return storageService.patch(collection, processedData);
  }

  @Override
  public <T extends EntityObject> long patch(Principal principal, String collection, String query, T data) throws ApplicationException
  {
    DataProcessor dataProcessor = dataProcessors.get(collection);
    T processedData = dataProcessor != null ? dataProcessor.handlePatch(principal, data) : data;
    return storageService.patch(collection, query, processedData);
  }

  @Override
  public long delete(Principal principal, String collection, String id) throws ApplicationException
  {
    DataProcessor dataProcessor = dataProcessors.get(collection);
    
    if(dataProcessor != null)
      dataProcessor.handleDelete(principal, id);
    
    return storageService.delete(collection, id);
  }

  @Override
  public long delete(Principal principal, String collection, String key, String value) throws ApplicationException
  {
    DataProcessor dataProcessor = dataProcessors.get(collection);
    
    if(dataProcessor != null)
      dataProcessor.handleDelete(principal, key, value);
    
    return storageService.delete(collection, key, value);
  }

  @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE)
  public void bindDataProcessor(DataProcessor dataProcessor, Map<String, Object> properties)
  {
    Object collectionProperty = properties.get("collection");
    
    if(collectionProperty == null)
      return;
    
    if(collectionProperty instanceof String)
    {
      dataProcessors.put((String) collectionProperty, dataProcessor);
    }
    else if(collectionProperty instanceof String[])
    {
      for(String collection : (String[]) collectionProperty)
        dataProcessors.put(collection, dataProcessor);
    }
  }

  public void unbindDataProcessor(DataProcessor dataProcessor, Map<String, Object> properties)
  {
    Object collectionProperty = properties.get("collection");
    
    if(collectionProperty == null)
      return;
    
    if(collectionProperty instanceof String)
    {
      dataProcessors.remove((String) collectionProperty);
    }
    else if(collectionProperty instanceof String[])
    {
      for(String collection : (String[]) collectionProperty)
        dataProcessors.remove(collection);
    }
  }

  @Reference(unbind = "-")
  public void bindEntitySecurityProvider(EntitySecurityProvider securityProvider)
  {
    this.securityProvider = securityProvider;
  }
  
  @Reference(unbind = "-")
  public void bindStorageSerivce(StorageService storageService)
  {
    this.storageService = storageService;
  }
}
