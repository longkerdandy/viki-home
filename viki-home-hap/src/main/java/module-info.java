module com.github.longkerdandy.viki.home.hap {

  // for jackson only
  opens com.github.longkerdandy.viki.home.hap.model to com.fasterxml.jackson.databind;
  opens com.github.longkerdandy.viki.home.hap.model.property to com.fasterxml.jackson.databind;
  opens com.github.longkerdandy.viki.home.hap.http.request to com.fasterxml.jackson.databind;
  opens com.github.longkerdandy.viki.home.hap.http.response to com.fasterxml.jackson.databind;

  // project api
  requires com.github.longkerdandy.viki.home.api;

  // database migration
  requires org.flywaydb.core;

  // mDNS
  requires jmdns;

  // encryption
  requires srp6a;             // TODO: Automatic Module Name
  requires hkdf;              // TODO: Automatic Module Name
  requires net.i2p.crypto.eddsa;
  requires curve25519.java;   // TODO: Automatic Module Name

  // netty
  requires io.netty.buffer;
  requires io.netty.codec;
  requires io.netty.codec.http;
  requires io.netty.common;
  requires io.netty.handler;
  requires io.netty.transport;

  // url utils
  requires org.apache.httpcomponents.httpcore;
  requires org.apache.httpcomponents.httpclient;

  // service
  provides com.github.longkerdandy.viki.home.ext.ControllerExtFactory
      with com.github.longkerdandy.viki.home.hap.HomeKitProtocolExtFactory;
}