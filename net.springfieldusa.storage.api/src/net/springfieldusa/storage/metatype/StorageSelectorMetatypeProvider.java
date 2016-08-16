package net.springfieldusa.storage.metatype;

import java.util.Map;

import org.eclipselabs.emeta.AttributeDefinitionImpl;
import org.eclipselabs.emeta.ServiceChoiceAttributeDefinitionImpl;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.metatype.MetaTypeProvider;

import net.springfieldusa.storage.StorageService;

public abstract class StorageSelectorMetatypeProvider implements MetaTypeProvider
{
  private ServiceChoiceAttributeDefinitionImpl storageAttribute;

  public StorageSelectorMetatypeProvider()
  {
    storageAttribute = new ServiceChoiceAttributeDefinitionImpl("StorageService", "Storage", "application");
    storageAttribute.setDescription("The storage service");
  }
  
  @Override
  public String[] getLocales()
  {
    return null;
  }

  public AttributeDefinitionImpl getStorageSelector()
  {

    return storageAttribute;
  }

  @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
  public void bindStorageService(StorageService storageService, Map<String, Object> properties)
  {
    storageAttribute.addService(properties);
  }

  public void unbindStorageService(StorageService storageService, Map<String, Object> properties)
  {
    storageAttribute.removeService(properties);
  }
}
