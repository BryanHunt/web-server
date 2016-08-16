package net.springfieldusa.users.comp;

import org.osgi.service.component.annotations.*;

import net.springfieldusa.comp.AbstractComponent;
import net.springfieldusa.storage.StorageService;
import net.springfieldusa.users.User;
import net.springfieldusa.users.UserException;
import net.springfieldusa.users.UserService;

@Component(service = UserService.class)
public class UsersComponent extends AbstractComponent implements UserService
{
  private volatile StorageService storageService;
  
  @Override
  public User getUser(String userId) throws UserException
  {
    return storageService.retrieve(User.COLLECTION, User.KEY_EMAIL, userId, User::new);
  }
  
  @Reference(unbind = "-")
  public void bindStorageService(StorageService storageService)
  {
    this.storageService = storageService;
  }
}
