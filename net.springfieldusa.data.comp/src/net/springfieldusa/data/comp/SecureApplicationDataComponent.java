package net.springfieldusa.data.comp;

import java.security.Principal;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import net.springfieldusa.comp.AbstractComponent;
import net.springfieldusa.data.ApplicationDataService;
import net.springfieldusa.data.ApplicationException;
import net.springfieldusa.data.AuthorizationException;
import net.springfieldusa.data.EntityAuthorizationService;
import net.springfieldusa.entity.EntityObject;
import net.springfieldusa.storage.DuplicateIdException;

@Component(service = ApplicationDataService.class, property = {"secure:Boolean=true"})
public class SecureApplicationDataComponent extends AbstractComponent implements ApplicationDataService
{
  private volatile EntityAuthorizationService entityAuthorizationService;
  private volatile ApplicationDataService applicationDataService;
  
  @Override
  public <T extends EntityObject> T create(Principal principal, String collection, T data) throws DuplicateIdException, ApplicationException, AuthorizationException
  {
    if(!entityAuthorizationService.isCreateAuthorizedFor(principal, collection, data))
      throw new AuthorizationException();
    
    return applicationDataService.create(principal, collection, data);
  }

  @Override
  public EntityObject retrieve(Principal principal, String collection, String id) throws ApplicationException, AuthorizationException
  {
    return retrieve(principal, collection, id, EntityObject::new);
  }

  @Override
  public EntityObject retrieve(Principal principal, String collection, String key, String value) throws ApplicationException, AuthorizationException
  {
    return retrieve(principal, collection, key, value, EntityObject::new);
  }

  @Override
  public <T extends EntityObject> T retrieve(Principal principal, String collection, String id, Supplier<T> factory) throws ApplicationException, AuthorizationException
  {
    T data = applicationDataService.retrieve(principal, collection, id, factory);
    
    if(!entityAuthorizationService.isRetrieveAuthorizedFor(principal, collection, data))
      throw new AuthorizationException();
    
    return data;
  }

  @Override
  public <T extends EntityObject> T retrieve(Principal principal, String collection, String key, String value, Supplier<T> factory) throws ApplicationException, AuthorizationException
  {
    T data = applicationDataService.retrieve(principal, collection, key, value, factory);
    
    if(!entityAuthorizationService.isRetrieveAuthorizedFor(principal, collection, data))
      throw new AuthorizationException();
    
    return data; 
  }

  @Override
  public Collection<EntityObject> find(Principal principal, String collection, Map<String, Object> query) throws ApplicationException, AuthorizationException
  {
    return find(principal, collection, query, EntityObject::new);
  }

  @Override
  public Collection<EntityObject> find(Principal principal, String collection, String query) throws ApplicationException, AuthorizationException
  {
    return find(principal, collection, query, EntityObject::new);
  }

  @Override
  public <T extends EntityObject> Collection<T> find(Principal principal, String collection, Map<String, Object> query, Supplier<T> factory) throws ApplicationException, AuthorizationException
  {
    Collection<T> data = applicationDataService.find(principal, collection, query, factory);
    
    Iterator<T> iterator = data.iterator();
    
    while(iterator.hasNext())
    {
      if(!entityAuthorizationService.isRetrieveAuthorizedFor(principal, collection, iterator.next()))
        iterator.remove();
    }
    
    return data;
  }

  @Override
  public <T extends EntityObject> Collection<T> find(Principal principal, String collection, String query, Supplier<T> factory) throws ApplicationException, AuthorizationException
  {
    Collection<T> data = applicationDataService.find(principal, collection, query, factory);
    
    Iterator<T> iterator = data.iterator();
    
    while(iterator.hasNext())
    {
      if(!entityAuthorizationService.isRetrieveAuthorizedFor(principal, collection, iterator.next()))
        iterator.remove();
    }
    
    return data;
  }

  @Override
  public Collection<EntityObject> find(Principal principal, String collection, Map<String, Object> query, int skip, int limit) throws ApplicationException, AuthorizationException
  {
    return find(principal, collection, query, skip, limit, EntityObject::new);
  }

  @Override
  public Collection<EntityObject> find(Principal principal, String collection, String query, int skip, int limit) throws ApplicationException, AuthorizationException
  {
    return find(principal, collection, query, skip, limit, EntityObject::new);
  }

  @Override
  public <T extends EntityObject> Collection<T> find(Principal principal, String collection, Map<String, Object> query, int skip, int limit, Supplier<T> factory) throws ApplicationException, AuthorizationException
  {
    Collection<T> data = applicationDataService.find(principal, collection, query, skip, limit, factory);
    
    Iterator<T> iterator = data.iterator();
    
    while(iterator.hasNext())
    {
      if(!entityAuthorizationService.isRetrieveAuthorizedFor(principal, collection, iterator.next()))
        iterator.remove();
    }
    
    return data;
  }

  @Override
  public <T extends EntityObject> Collection<T> find(Principal principal, String collection, String query, int skip, int limit, Supplier<T> factory) throws ApplicationException, AuthorizationException
  {
    Collection<T> data = applicationDataService.find(principal, collection, query, skip, limit, factory);
    
    Iterator<T> iterator = data.iterator();
    
    while(iterator.hasNext())
    {
      if(!entityAuthorizationService.isRetrieveAuthorizedFor(principal, collection, iterator.next()))
        iterator.remove();
    }
    
    return data;
  }

  @Override
  public <T extends EntityObject> long update(Principal principal, String collection, T data) throws ApplicationException, AuthorizationException
  {
    EntityObject storedObject = applicationDataService.retrieve(principal, collection, data.getId());
    
    if(!entityAuthorizationService.isUpdateAuthorizedFor(principal, collection, data, storedObject))
      throw new AuthorizationException();
    
    return applicationDataService.update(principal, collection, data);
  }

  @Override
  public <T extends EntityObject> long update(Principal principal, String collection, String query, T data) throws ApplicationException, AuthorizationException
  {
    EntityObject storedObject = applicationDataService.retrieve(principal, collection, data.getId());

    if(!entityAuthorizationService.isUpdateAuthorizedFor(principal, collection, data, storedObject))
      throw new AuthorizationException();
    
    return applicationDataService.update(principal, collection, query, data);
  }

  @Override
  public <T extends EntityObject> long patch(Principal principal, String collection, T data) throws ApplicationException, AuthorizationException
  {
    EntityObject storedObject = applicationDataService.retrieve(principal, collection, data.getId());

    if(!entityAuthorizationService.isUpdateAuthorizedFor(principal, collection, data, storedObject))
      throw new AuthorizationException();
    
    return applicationDataService.patch(principal, collection, data);
  }

  @Override
  public <T extends EntityObject> long patch(Principal principal, String collection, String query, T data) throws ApplicationException, AuthorizationException
  {
    EntityObject storedObject = applicationDataService.retrieve(principal, collection, data.getId());

    if(!entityAuthorizationService.isUpdateAuthorizedFor(principal, collection, data, storedObject))
      throw new AuthorizationException();
    
    return applicationDataService.patch(principal, collection, data);
  }

  @Override
  public long delete(Principal principal, String collection, String id) throws ApplicationException, AuthorizationException
  {
    EntityObject data = applicationDataService.retrieve(principal, collection, id);
    
    if(!entityAuthorizationService.isDeleteAuthorizedFor(principal, collection, data))
      throw new AuthorizationException();
    
    return applicationDataService.delete(principal, collection, id);    
  }

  @Override
  public long delete(Principal principal, String collection, String key, String value) throws ApplicationException, AuthorizationException
  {
    EntityObject data = applicationDataService.retrieve(principal, collection, key, value);
    
    if(!entityAuthorizationService.isDeleteAuthorizedFor(principal, collection, data))
      throw new AuthorizationException();
    
    return applicationDataService.delete(principal, collection, key, value);    
  }

  @Reference(unbind = "-", target = "(secure=false)")
  public void bindApplicationDataSerivce(ApplicationDataService applicationDataService)
  {
    this.applicationDataService = applicationDataService;
  }
  
  @Reference(unbind = "-")
  public void bindEntityAuthorizationService(EntityAuthorizationService entityAuthorizationService)
  {
    this.entityAuthorizationService = entityAuthorizationService;
  }
}
