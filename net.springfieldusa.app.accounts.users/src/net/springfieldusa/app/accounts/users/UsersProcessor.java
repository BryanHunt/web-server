package net.springfieldusa.app.accounts.users;

import java.security.Principal;
import java.util.Date;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import net.springfieldusa.comp.AbstractComponent;
import net.springfieldusa.credentials.CredentialException;
import net.springfieldusa.credentials.CredentialsService;
import net.springfieldusa.credentials.UnencryptedCredential;
import net.springfieldusa.data.ApplicationException;
import net.springfieldusa.data.DataProcessor;
import net.springfieldusa.entity.EntityObject;
import net.springfieldusa.storage.StorageService;
import net.springfieldusa.users.User;

@Component(service = DataProcessor.class, property = {"collection=users"})
public class UsersProcessor extends AbstractComponent implements DataProcessor
{
  private volatile CredentialsService credentialsService;
  private volatile StorageService storageService;
  
  @Override
  public <T extends EntityObject> T handleCreate(Principal principal, T data) throws ApplicationException
  {
    User user = new User(data);
    String password = user.clearPassword();
    
    try
    {
      if(password != null)
        credentialsService.addCredential(new UnencryptedCredential((String) user.getEmail(), password));
      
      user.setCreatedOn(new Date());
      return data;
    }
    catch (CredentialException e)
    {
      log(LogService.LOG_ERROR, "Failed to add user", e);
      throw new ApplicationException(e);
    }
  }

  @Override
  public <T extends EntityObject> T handleUpdate(Principal principal, T data) throws ApplicationException
  {
    User user = new User(data);
    User dbUser = storageService.retrieve(User.COLLECTION, user.getId(), User::new);
    
    if(dbUser == null)
      throw new ApplicationException("Invalid user");
    
    String password = user.clearPassword();
    
    try
    {
      if(password != null)
        credentialsService.updateCredential(new UnencryptedCredential(user.getEmail(), password));
      
      return data;
    }
    catch (CredentialException e)
    {
      log(LogService.LOG_ERROR, "Failed to add user", e);
      throw new ApplicationException(e);
    }
  }

  @Override
  public <T extends EntityObject> T handlePatch(Principal principal, T data) throws ApplicationException
  {
    return handleUpdate(principal, data);
  }

  @Override
  public <T extends EntityObject> void handleDelete(Principal principal, String id) throws ApplicationException
  {
    User user = storageService.retrieve("users", id, User::new);

    try
    {
      credentialsService.removeCredential(user.getEmail());
    }
    catch (CredentialException e)
    {
      log(LogService.LOG_ERROR, "Failed to remove user", e);
      throw new ApplicationException(e);
    }
  }
  
  @Override
  public <T extends EntityObject> void handleDelete(Principal principal, String key, String value) throws ApplicationException
  {
    User user = storageService.retrieve("users", key, value, User::new);

    try
    {
      credentialsService.removeCredential(user.getEmail());
    }
    catch (CredentialException e)
    {
      log(LogService.LOG_ERROR, "Failed to remove user", e);
      throw new ApplicationException(e);
    }
  }

  @Reference(unbind = "-")
  public void bindCredentialsService(CredentialsService credentialsService)
  {
    this.credentialsService = credentialsService;
  }
  
  @Reference(unbind = "-")
  public void bindStorageService(StorageService storageService)
  {
    this.storageService = storageService;
  }
}
