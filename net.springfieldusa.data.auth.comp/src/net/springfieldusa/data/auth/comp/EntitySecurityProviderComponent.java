package net.springfieldusa.data.auth.comp;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import net.springfieldusa.comp.AbstractComponent;
import net.springfieldusa.data.ApplicationException;
import net.springfieldusa.data.EntitySecurityProvider;
import net.springfieldusa.entity.EntityObject;
import net.springfieldusa.security.SecurityException;
import net.springfieldusa.security.SecurityService;

@Component(service = {EntitySecurityProvider.class, EntitySecurityProviderComponent.class})
public class EntitySecurityProviderComponent extends AbstractComponent implements EntitySecurityProvider
{
  public @interface Config
  {
    String adminGroup() default "admin";
    String securityKey() default "security";
    boolean useMeta() default true;
  }
  
  private volatile SecurityService securityService;
  private String adminGroup;
  private String securityKey;
  private boolean useMeta;
  
  @Activate
  public void activate(Config config)
  {
    adminGroup = config.adminGroup();
    securityKey = config.securityKey();
    useMeta = config.useMeta();
  }

  @SuppressWarnings("unchecked")
  public ObjectSecurity getObjectSecurity(EntityObject data) throws ApplicationException
  {
    Map<String, Object> securityAttributes = null;
    
    if(useMeta)
    {
      Map<String, Object> meta = data.getMeta();
      
      if(meta == null)
        return null;

      securityAttributes = (Map<String, Object>) meta.get(securityKey);
    }
    else
    {
      securityAttributes = (Map<String, Object>) data.getAttributes().get(securityKey);      
    }

    return securityAttributes != null ? new ObjectSecurity(securityAttributes) : null;
  }

  @Override
  public void setObjectSecurity(EntityObject data, Principal principal) throws ApplicationException
  {
    try
    {
      ObjectSecurity security = getObjectSecurity(data);
      
      if (security == null)
      {
        security = new ObjectSecurity();
        security.setOwner(principal.getName());
        
        if(useMeta)
        {
          Map<String, Object> meta = data.getMeta();

          if(meta == null)
          {
            meta = new HashMap<>();
            data.setMeta(meta);
          }

          meta.put(securityKey, security.getAttributes());
        }
        else
        {
          data.getAttributes().put(securityKey, security.getAttributes());
        }
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
