package com.telsoft.cbs.camel.service.configuration;

import com.telsoft.cbs.camel.service.ServiceAdapter;
import com.telsoft.dictionary.Dictionary;
import com.telsoft.dictionary.DictionaryNode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.Connection;

@Slf4j
public class ConfigService extends ServiceAdapter {

    private static final String CONFIGURATION_ROOT = "./configuration/";

    @Override
    protected void resetModifyFlag(Connection connection) {

    }

    @Override
    protected boolean hasModified(Connection connection) {
        return false;
    }

    @Override
    protected void doSync(Connection connection) {

    }

    @Override
    public void init() {

    }

    @Override
    public void close() {

    }

    private String formatFileName(String filename) {
        return CONFIGURATION_ROOT + filename;
    }

    public DictionaryNode getConfig(String filename) {
        try {
            return new Dictionary(formatFileName(filename));
        } catch (IOException e) {
            log.error("Load config {} error", filename, e);
            return null;
        }
    }
}
