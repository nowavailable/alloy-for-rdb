package com.testdatadesigner.tdalloy.core.conversation;

public class IllegalParameterError extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public IllegalParameterError() {
    super();
  }

  public IllegalParameterError(String message) {
    super(message);
  }

}
