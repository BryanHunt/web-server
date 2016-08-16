package net.springfieldusa.data.comp;

import java.security.Principal;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import net.springfieldusa.comp.AbstractComponent;
import net.springfieldusa.data.ApplicationException;
import net.springfieldusa.data.EntitySecurityProvider;
import net.springfieldusa.entity.EntityObject;
import net.springfieldusa.entity.ObjectSecurity;
import net.springfieldusa.security.SecurityException;
import net.springfieldusa.security.SecurityService;

@Component(service = EntitySecurityProvider.class)
public class EntitySecurityProviderComponent extends AbstractComponent implements EntitySecurityProvider
{
  public @interface Config
  {
    String adminGroup() default "admin";
  }
  
  private volatile SecurityService securityService;
  private String adminGroup;
  
  @Activate
  public void activate(Config config)
  {
    adminGroup = config.adminGroup();
  }

  @Override
  public void setObjectSecurity(EntityObject data, Principal principal) throws ApplicationException
  {
    try
    {
      ObjectSecurity security = data.getSecurity();

      if (security == null)
      {
        security = new ObjectSecurity();
        security.setOwner(principal.getName());
        data.setSecurity(security);
      }
      else if (!securityService.authorizeForRole(principal, adminGroup))
      {
        security.setOwner(principal.getName());
      }
    }
    catch (SecurityException e)
    {
      throw new ApplicationException(e);
    }
  }

  @Reference(unbind = "-")
  public void bindSecurityService(SecurityService securityService)
  {
    this.securityService = securityService;
  }
}
