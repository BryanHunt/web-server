package net.springfieldusa.storage.mongodb.meta;

import org.eclipselabs.emongo.metatype.DatabaseSelectorMetatypeProvider;
import org.eclipselabs.emongo.metatype.ObjectClassDefinitionImpl;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.MetaTypeProvider;
import org.osgi.service.metatype.ObjectClassDefinition;

@Component(service = MetaTypeProvider.class, property = {"metatype.factory.pid=net.springfieldusa.storage.mongodb.comp.MongoStorageComponent"})
public class MongoStorageComponentMetatypeProvider extends DatabaseSelectorMetatypeProvider
{
  @Override
  public ObjectClassDefinition getObjectClassDefinition(String id, String locale)
  {
    ObjectClassDefinitionImpl ocd = new ObjectClassDefinitionImpl("net.springfieldusa.storage.mongodb.comp.MongoStorageComponent", "Mongo Storage", "MongoDB Storage Component Configuration");
    ocd.addRequiredAttribute(createDatabaseSelector());
    
    return ocd;
  }
}
