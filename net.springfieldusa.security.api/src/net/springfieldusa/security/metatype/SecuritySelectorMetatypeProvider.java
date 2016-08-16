package net.springfieldusa.security.metatype;

import java.util.Map;

import org.eclipselabs.emeta.AttributeDefinitionImpl;
import org.eclipselabs.emeta.ServiceChoiceAttributeDefinitionImpl;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.metatype.MetaTypeProvider;

import net.springfieldusa.security.SecurityService;

public abstract class SecuritySelectorMetatypeProvider implements MetaTypeProvider
{
  private ServiceChoiceAttributeDefinitionImpl securityAttribute;
  
  public SecuritySelectorMetatypeProvider()
  {
    securityAttribute = new ServiceChoiceAttributeDefinitionImpl("SecurityService", "Security", "application");
    securityAttribute.setDescription("The security service");
  }
  
  @Override
  public String[] getLocales()
  {
    return null;
  }

  public AttributeDefinitionImpl getSecuritySelector()
  {
    return securityAttribute;
  }

  @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
  public void bindSecurityService(SecurityService storageService, Map<String, Object> properties)
  {
    securityAttribute.addService(properties);
  }

  public void unbindSecurityService(SecurityService storageService, Map<String, Object> properties)
  {
    securityAttribute.removeService(properties);
  }
}
