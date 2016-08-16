package net.springfieldusa.storage;

public class DuplicateIdException extends Exception
{
  private static final long serialVersionUID = 3663732135215318570L;

  public DuplicateIdException()
  {
  }

  public DuplicateIdException(String message)
  {
    super(message);
  }

  public DuplicateIdException(Throwable cause)
  {
    super(cause);
  }

  public DuplicateIdException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public DuplicateIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
  {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
