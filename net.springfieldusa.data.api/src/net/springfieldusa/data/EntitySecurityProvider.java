package net.springfieldusa.data;

import java.security.Principal;

import net.springfieldusa.entity.EntityObject;

public interface EntitySecurityProvider
{
  void setObjectSecurity(EntityObject data, Principal principal) throws ApplicationException;
}
