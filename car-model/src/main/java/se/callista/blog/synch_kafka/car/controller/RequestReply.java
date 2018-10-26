package se.callista.blog.synch_kafka.car.controller;

public class RequestReply {
  private Object request;
  private Object reply;

  public Object getRequest() {
    return request;
  }

  public Object getReply() {
    return reply;
  }

  public static RequestReply request(Object request) {
    RequestReply requestReply = new RequestReply();
    requestReply.request = request;
    return requestReply;
  }

  public static RequestReply reply(Object reply) {
    RequestReply requestReply = new RequestReply();
    requestReply.reply = reply;
    return requestReply;
  }

}
