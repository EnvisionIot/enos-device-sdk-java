package com.envisioniot.enos.iot_mqtt_sdk.core.profile;

import com.envisioniot.enos.iot_mqtt_sdk.core.login.LoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.NormalDeviceLoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.util.GsonUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class FileProfile extends BaseProfile {

    private Logger logger = LoggerFactory.getLogger(FileProfile.class);
    private File configFile;

    public FileProfile() {
        this(".config");
    }

    public FileProfile(String filePath) {
        this(filePath, null);
    }

    public FileProfile(String filePath, LoginInput input) {
        this.configFile = new File(filePath);
        reload();

        if (input != null) {
            this.config.setServerUrl(input.getServerUrl());
            this.config.setProductKey(input.getProductKey());
            this.config.setProductSecret(input.getProductSecret());
            this.config.setDeviceKey(input.getDeviceKey());
            this.config.setDeviceSecret(input.getDeviceSecret());
        }
    }

    @Override
    public void reload() {
        if (configFile.exists()) {
            try {
                String json = Files.toString(configFile, Charsets.UTF_8);
                Config conf = GsonUtil.fromJson(json, Config.class);
                if (conf != null) {
                    this.config = conf;
                }
            } catch (IOException e) {
                logger.error("failed to load file [{}] profile", configFile.getAbsolutePath(), e);
            }
        }
    }

    public void persist(String filePath) throws IOException {
        config.store(filePath, "store config by mqtt sdk");
    }

    public void persist() throws IOException {
        persist(configFile.getPath());
    }

    public static void main(String[] args) {
        LoginInput input = new NormalDeviceLoginInput("url", "pk", "dk", "secret");
        FileProfile profile = new FileProfile("C:\\Users\\jian.zhang4\\projects\\.config", input);
        try {
            profile.persist();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
