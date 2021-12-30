package com.maplecloudy.osrt.model.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Hard {
  public long memory = 1024;
  public int virtualCores = 1;
  String[] nodes = null;
  String[] racks = null;
  int priority = 1;
  HashMap<String,Object> configMap = Maps.newHashMap();
  
  public void setNode(String node) {
    this.nodes = new String[] {node};
  }
  
  public long getMemory() {
    return memory;
  }
  
  public int getVirtualCores() {
    return virtualCores;
  }
  
  public void setVirtualCores(int virtualCores) {
    this.virtualCores = virtualCores;
  }
  
  public String[] getNodes() {
    return nodes;
  }
  
  public void setNodes(String[] nodes) {
    this.nodes = nodes;
  }
  
  public String[] getRacks() {
    return racks;
  }
  
  public void setRacks(String[] racks) {
    this.racks = racks;
  }
  
  public int getPriority() {
    return priority;
  }
  
  public void setPriority(int priority) {
    this.priority = priority;
  }
  
  public HashMap<String,Object> getConfigMap() {
    return configMap;
  }
  
  public void setConfigMap(HashMap<String,Object> configMap) {
    this.configMap = configMap;
  }
  

  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(nodes);
    result = prime * result + Arrays.hashCode(racks);
    result = prime * result
        + Objects.hash(configMap, memory, priority, virtualCores);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Hard other = (Hard) obj;
    return Objects.equals(configMap, other.configMap) && memory == other.memory
        && Arrays.equals(nodes, other.nodes) && priority == other.priority
        && Arrays.equals(racks, other.racks)
        && virtualCores == other.virtualCores;
  }

  public void setMemory(long memory) {
    this.memory = memory;
  }

  public Hard() {
  }

  public Hard(long memory, int virtualCores) {
    this.memory = memory;
    this.virtualCores = virtualCores;
  }
}
