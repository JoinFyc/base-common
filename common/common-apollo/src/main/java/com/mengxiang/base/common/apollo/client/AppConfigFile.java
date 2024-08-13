package com.mengxiang.base.common.apollo.client;

import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;

/**
 * 配置文件对象
 *
 * @author ice
 * @version 1.0
 * @date 2019/4/23 9:37 PM
 */
public class AppConfigFile {

    private ConfigFile configFile;

    public AppConfigFile(ConfigFile configFile) {
        this.configFile = configFile;
    }

    public String getContent() {
        return configFile.getContent();
    }

    public boolean hasContent() {
        return configFile.hasContent();
    }

    public String getNamespace() {
        return configFile.getNamespace();
    }

    public FileFormat getFileFormat() {
        ConfigFileFormat configFileFormat = configFile.getConfigFileFormat();
        FileFormat fileFormat = FileFormat.valueOf(configFileFormat.toString());
        return fileFormat;
    }
}
