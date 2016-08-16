package net.springfieldusa.jwt.jjwt.comp;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import net.springfieldusa.jwt.ClaimsProvider;

@Component(service = ClaimsProvider.class, property = {Constants.SERVICE_RANKING + ":Integer=-1"})
public class ClaimsProviderComponent implements ClaimsProvider
{
  public @interface Config
  {
    long tokenExpirationAmount() default 1;
    String tokenExpirationUnit() default "DAYS";
  }
 
  private long tokenExpirationAmount;
  private ChronoUnit tokenExpirationUnit;
  
  @Activate
  public void activate(Config config)
  {
    tokenExpirationAmount = config.tokenExpirationAmount();
    tokenExpirationUnit = ChronoUnit.valueOf(config.tokenExpirationUnit());
  }
  
  @Override
  public Map<String, Object> getClaims(Principal principal)
  {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", principal.getName());
    claims.put("exp", Instant.now().plus(tokenExpirationAmount, tokenExpirationUnit).getEpochSecond());
    return claims;
  }
}
