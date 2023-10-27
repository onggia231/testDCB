package com.telsoft.cbs.module.fortumo.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class HzClient {

    private String cluster;
    private String mapName;
    private int ttl;
    private IMap map;
    private HazelcastInstance hazelcastInstance;

    public HzClient() {
    }

    public boolean checkCluster() {
        try {
            hazelcastInstance.getMap(mapName).get("");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public int checkAvailable(String key) {
        if (checkCluster()) {
            Object value = map.get(key);
            if (value == null) {
                return 0;//req moi chua xu ly
            } else {
                if (("").equals(value.toString())) {
                    return 1;//Dang xu ly
                } else {
                    return 2;//có trong cache da xu ly xong
                }
            }
        } else return 99;//cluster chet roi
    }

    public Object get(String key) {
        return map.get(key);
    }

    public Object get(int key) {
        return map.get(key);
    }

    public void put(String key, String value) {
        map.put(key, value, this.ttl, TimeUnit.SECONDS);
    }

    public void put(int key, String value) {
        map.put(key, value, this.ttl, TimeUnit.SECONDS);
    }

    public void put(String key, Object value) {
        map.put(key, value, this.ttl, TimeUnit.SECONDS);
    }

    public void put(int key, Object value) {
        map.put(key, value, this.ttl, TimeUnit.SECONDS);
    }
}
