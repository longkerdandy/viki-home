package com.github.longkerdandy.viki.home.hap.http.response;

/**
 * Response for error
 */
public class ErrorResponse {

  private Status status;

  private ErrorResponse() {
  }

  public ErrorResponse(Status status) {
    this.status = status;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }
}
