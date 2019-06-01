package com.github.longkerdandy.viki.home.hap.model;

import java.util.Arrays;

/**
 * HomeKit Accessory Protocol Bridge
 */
public class Bridge {

  private Long instanceId;
  private Integer configNum;
  private Integer stateNum;
  private String protocolVersion;
  private Integer statusFlag;
  private Integer categoryId;
  private byte[] privateKey;
  private byte[] publicKey;

  private Bridge() {
  }

  public Bridge(Long instanceId, Integer configNum, Integer stateNum, String protocolVersion,
      Integer statusFlag, Integer categoryId, byte[] privateKey, byte[] publicKey) {
    this.instanceId = instanceId;
    this.configNum = configNum;
    this.stateNum = stateNum;
    this.protocolVersion = protocolVersion;
    this.statusFlag = statusFlag;
    this.categoryId = categoryId;
    this.privateKey = privateKey;
    this.publicKey = publicKey;
  }

  public Long getInstanceId() {
    return instanceId;
  }

  public Integer getConfigNum() {
    return configNum;
  }

  public Integer getStateNum() {
    return stateNum;
  }

  public String getProtocolVersion() {
    return protocolVersion;
  }

  public Integer getStatusFlag() {
    return statusFlag;
  }

  public Integer getCategoryId() {
    return categoryId;
  }

  public byte[] getPrivateKey() {
    return privateKey;
  }

  public byte[] getPublicKey() {
    return publicKey;
  }

  @Override
  public String toString() {
    return "Bridge{" +
        "instanceId=" + instanceId +
        ", configNum=" + configNum +
        ", stateNum=" + stateNum +
        ", protocolVersion='" + protocolVersion + '\'' +
        ", statusFlag=" + statusFlag +
        ", categoryId=" + categoryId +
        ", privateKey=" + Arrays.toString(privateKey) +
        ", publicKey=" + Arrays.toString(publicKey) +
        '}';
  }
}
