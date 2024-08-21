package com.fei.base.common.apollo.client;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;

/**
 * 应用配置工具类
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/4/19 9:34 PM
 */
public final class AppConfigUtils {

    private AppConfigUtils() {
    }

    public static String getProperty(String namespace, String key, String defaultValue) {
        Config config = ConfigService.getConfig(namespace);
        return config.getProperty(key, defaultValue);
    }

    public static String getProperty(String namespace, String key) {
        return getProperty(namespace, key, null);
    }

    public static AppConfig getConfig(String namespace) {
        Config config = ConfigService.getConfig(namespace);
        return new AppConfig(config);
    }

    public static AppConfigFile getConfigFile(String namespace, FileFormat fileFormat) {
        ConfigFileFormat configFileFormat = transfer(fileFormat);
        ConfigFile configFile = ConfigService.getConfigFile(namespace, configFileFormat);
        return new AppConfigFile(configFile);
    }

    private static ConfigFileFormat transfer(FileFormat format) {
        switch (format) {
            case Properties:
                return ConfigFileFormat.Properties;
            case YML:
                return ConfigFileFormat.YML;
            case YAML:
                return ConfigFileFormat.YAML;
            case XML:
                return ConfigFileFormat.XML;
            case JSON:
                return ConfigFileFormat.JSON;
            default:
                return null;
        }
    }
}
