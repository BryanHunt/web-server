package net.springfieldusa.jwt;

public class TokenExpiredException extends TokenException
{
  private static final long serialVersionUID = -479377812435162475L;

  public TokenExpiredException()
  {
    super();
  }

  public TokenExpiredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
  {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public TokenExpiredException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public TokenExpiredException(String message)
  {
    super(message);
  }

  public TokenExpiredException(Throwable cause)
  {
    super(cause);
  }
}
