package com.api.account.model;

public class Message {

  private boolean success;
  private String description;

  public Message() {}

  public Message(final boolean success, final String description) {
    this.success = success;
    this.description = description;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getDescription() {
    return description;
  }
}
