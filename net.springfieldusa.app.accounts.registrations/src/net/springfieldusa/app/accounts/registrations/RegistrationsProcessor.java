package net.springfieldusa.app.accounts.registrations;

import java.security.Principal;
import java.util.Collection;
import java.util.Date;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import net.springfieldusa.comp.AbstractComponent;
import net.springfieldusa.data.ApplicationException;
import net.springfieldusa.data.DataProcessor;
import net.springfieldusa.entity.EntityObject;
import net.springfieldusa.password.EncryptionException;
import net.springfieldusa.password.PasswordService;
import net.springfieldusa.users.User;

@Component(service = DataProcessor.class, property = { "collection=registrations" })
public class RegistrationsProcessor extends AbstractComponent implements DataProcessor
{
  String KEY_ENCRYPTED_PASSWORD = "encryptedPassword";
  String KEY_SALT = "salt";
  String KEY_REGISTERED_ON = "registeredOn";

  private volatile PasswordService passwordService;

  @Override
  public <T extends EntityObject> T handleCreate(Principal principal, T data) throws ApplicationException
  {
    // TODO : See if the user is already registered or has a registration
    // pending

    User user = new User(data);

    try
    {
      String password = user.clearPassword();

      if (password == null)
        throw new ApplicationException("Invalid password");

      byte[] salt = passwordService.createSalt();
      user.getAttributes().put(KEY_SALT, salt);
      user.getAttributes().put(KEY_ENCRYPTED_PASSWORD, passwordService.encryptPassword(password, salt));
      user.getAttributes().put(KEY_REGISTERED_ON, new Date());

      log(LogService.LOG_DEBUG, "Registering user: '" + user.getEmail() + "'");
      return data;
    }
    catch (EncryptionException e)
    {
      log(LogService.LOG_ERROR, "Failed to register user", e);
      throw new ApplicationException(e);
    }
  }

  @Override
  public <T extends EntityObject> T handleRetrieve(Principal principal, T data) throws ApplicationException
  {
    return data;
  }

  @Override
  public <T extends EntityObject> Collection<T> handleRetrieve(Principal principal, Collection<T> data) throws ApplicationException
  {
    return data;
  }

  @Override
  public <T extends EntityObject> T handleUpdate(Principal principal, T data) throws ApplicationException
  {
    throw new ApplicationException("Updating a registration is not allowed");
  }

  @Override
  public <T extends EntityObject> T handlePatch(Principal principal, T data) throws ApplicationException
  {
    return handleUpdate(principal, data);
  }

  @Override
  public <T extends EntityObject> void handleDelete(Principal principal, String id) throws ApplicationException
  {
  }

  @Override
  public <T extends EntityObject> void handleDelete(Principal principal, String key, String value) throws ApplicationException
  {
  }

  @Reference(unbind = "-")
  public void bindPasswordService(PasswordService passwordService)
  {
    this.passwordService = passwordService;
  }
}
