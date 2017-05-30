package net.springfieldusa.data.auth.comp;

import java.security.Principal;
import java.util.Collection;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import net.springfieldusa.comp.AbstractComponent;
import net.springfieldusa.data.ApplicationException;
import net.springfieldusa.data.EntityAuthorizationService;
import net.springfieldusa.entity.EntityObject;
import net.springfieldusa.security.SecurityException;
import net.springfieldusa.security.SecurityService;

@Component(service = EntityAuthorizationService.class, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class EntityAuthorizationComponent extends AbstractComponent implements EntityAuthorizationService
{
  public @interface Config
  {
    boolean missingSecurityAuthorization() default false;
    String adminGroup() default "admin";
  }

  private String adminGroup;
  private boolean missingSecurityAuthorization;
  private volatile SecurityService securityService;
  private volatile EntitySecurityProviderComponent securityProvider;
  
  @Activate
  public void activate(Config config)
  {
    adminGroup = config.adminGroup();
    missingSecurityAuthorization = config.missingSecurityAuthorization();
  }

  @Override
  public boolean isCreateAuthorizedFor(Principal principal, String collection, EntityObject object)
  {
    return principal != null;
  }

  @Override
  public boolean isRetrieveAuthorizedFor(Principal principal, String collection, EntityObject object)
  {
    if (principal == null)
      return false;

    try
    {
      if (securityService.authorizeForRole(principal, adminGroup))
        return true;

      ObjectSecurity security = securityProvider.getObjectSecurity(object);

      if (security == null)
        return missingSecurityAuthorization;

      return security.isOwner(principal.getName()) || security.isReadAllowedFor(securityService.getRoles(principal));
    }
    catch (SecurityException | ApplicationException e)
    {
      log(LogService.LOG_ERROR, "Failed to authorize", e);
      return false;
    }
  }

  @Override
  public boolean isRetrieveAuthorizedFor(Principal principal, String collection, Collection<? extends EntityObject> objects)
  {
    if (principal == null)
      return false;

    try
    {
      if (securityService.authorizeForRole(principal, adminGroup))
        return true;

      for (EntityObject object : objects)
      {
        ObjectSecurity security = securityProvider.getObjectSecurity(object);

        if (security == null)
          return missingSecurityAuthorization;

        if (!security.isOwner(principal.getName()) && !security.isReadAllowedFor(securityService.getRoles(principal)))
          return false;
      }
    }
    catch (SecurityException | ApplicationException e)
    {
      log(LogService.LOG_ERROR, "Failed to authorize", e);
      return false;
    }

    return true;
  }

  @Override
  public boolean isUpdateAuthorizedFor(Principal principal, String collection, EntityObject updatedObject, EntityObject storedObject)
  {
    if (principal == null)
      return false;

    try
    {
      if (securityService.authorizeForRole(principal, adminGroup))
        return true;

      ObjectSecurity security = securityProvider.getObjectSecurity(storedObject);

      if (security == null)
        return missingSecurityAuthorization;

      return security.isOwner(principal.getName()) || security.isWriteAllowedFor(securityService.getRoles(principal));
    }
    catch (SecurityException | ApplicationException e)
    {
      log(LogService.LOG_ERROR, "Failed to authorize", e);
      return false;
    }
  }

  @Override
  public boolean isDeleteAuthorizedFor(Principal principal, String collection, EntityObject object)
  {
    if (principal == null)
      return false;

    try
    {
      if (securityService.authorizeForRole(principal, adminGroup))
        return true;

      ObjectSecurity security = securityProvider.getObjectSecurity(object);

      if (security == null)
        return missingSecurityAuthorization;

      return security.isOwner(principal.getName()) || security.isDeleteAllowedFor(securityService.getRoles(principal));
    }
    catch (SecurityException | ApplicationException e)
    {
      log(LogService.LOG_ERROR, "Failed to authorize", e);
      return false;
    }
  }

  @Reference(unbind = "-")
  public void bindSecurityService(SecurityService securityService)
  {
    this.securityService = securityService;
  }
  
  @Reference(unbind = "-")
  public void bindEntitySecurityProviderComponent(EntitySecurityProviderComponent securityProvider)
  {
    this.securityProvider = securityProvider;
  }
}
