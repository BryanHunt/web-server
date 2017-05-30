package net.springfieldusa.data.auth.comp;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import net.springfieldusa.comp.AbstractComponent;
import net.springfieldusa.data.ApplicationException;
import net.springfieldusa.data.EntitySecurityProvider;
import net.springfieldusa.entity.EntityObject;

@Component(service = {EntitySecurityProvider.class, EntitySecurityProviderComponent.class})
public class EntitySecurityProviderComponent extends AbstractComponent implements EntitySecurityProvider
{
  public @interface Config
  {
    String securityKey() default "security";
    boolean useMeta() default true;
  }
  
  private String securityKey;
  private boolean useMeta;
  
  @Activate
  public void activate(Config config)
  {
    securityKey = config.securityKey();
    useMeta = config.useMeta();
  }

  @SuppressWarnings("unchecked")
  public ObjectSecurity getObjectSecurity(EntityObject data) throws ApplicationException
  {
    if(data == null)
      return null;
    
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
  public void createObjectSecurity(EntityObject data, Principal principal) throws ApplicationException
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
  }
}
