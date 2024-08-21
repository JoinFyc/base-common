package com.fei.base.common.apollo.client.listener;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Apollo配置变化监听器注册对象，该对象为单例
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/4/22 8:22 PM
 */
public class BaseApolloConfigListenerRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseApolloConfigListenerRegistry.class);

    private static final BaseApolloConfigListenerRegistry REGISTRY =
            new BaseApolloConfigListenerRegistry();

    public static final String ALL_NAMESPACE = "*";

    private Map<BaseApolloConfigListener, ConfigChangeListener> listeners = Maps.newHashMap();

    private List<String> namespaces;

    private BaseApolloConfigListenerRegistry() {
        namespaces = new ArrayList<>();
    }

    public static BaseApolloConfigListenerRegistry getInstance() {
        return REGISTRY;
    }

    /**
     * 增加监听器
     *
     * @param listener
     */
    public void addListener(BaseApolloConfigListener listener) {
        if (!listeners.containsKey(listener)) {
            // 支持传入多个namespace，逗号分隔
            String namespaces = listener.namespace();
            String[] namespaceArray;
            if (namespaces == null || ALL_NAMESPACE.equals(namespaces)) {
                namespaceArray = new ArrayList<>(this.namespaces).toArray(new String[0]);
            } else {
                namespaceArray = namespaces.split(",");
            }
            for (String namespace : namespaceArray) {
                Config config = ConfigService.getConfig(namespace.trim());
                if (config != null) {
                    setListerner(config, listener);
                } else {
                    LOGGER.warn("namespace:{} 不存在", namespace);
                }
            }
        } else {
            LOGGER.warn("监听器{}已注册，不能重复注册", listener.getClass().toString());
        }
    }

    /**
     * 删除监听器
     *
     * @param listener
     */
    public void removeListener(BaseApolloConfigListener listener) {
        ConfigChangeListener originListener = listeners.remove(listener);
        if (originListener != null) {
            String namespace = listener.namespace();
            Config config = ConfigService.getConfig(namespace);
            if (config != null) {
                config.removeChangeListener(originListener);
            }
        } else {
            LOGGER.warn("监听器未注册");
        }
    }

    public List<String> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<String> namespaces) {
        this.namespaces = namespaces;
    }

    private void setListerner(Config config, BaseApolloConfigListener listener) {
        ConfigChangeListener originListener = new ConfigChangeListener() {
            @Override
            public void onChange(ConfigChangeEvent event) {
                BaseApolloConfigChangeEvent newEvent = buildEvent(event);
                listener.onConfigChange(newEvent);
            }
        };
        listeners.put(listener, originListener);
        config.addChangeListener(originListener);
    }

    /**
     * 构造变更事件
     *
     * @param event
     * @return
     */
    private BaseApolloConfigChangeEvent buildEvent(ConfigChangeEvent event) {

        BaseApolloConfigChangeEvent newEvent = new BaseApolloConfigChangeEvent();

        Map<String, BaseApolloConfigChange> changeMap = Maps.newHashMap();
        for (String key : event.changedKeys()) {
            ConfigChange change = event.getChange(key);
            if (change != null) {
                BaseApolloConfigChange newChange = new BaseApolloConfigChange();
                newChange.setNamespace(change.getNamespace());
                newChange.setKey(change.getPropertyName());
                newChange.setOldValue(change.getOldValue());
                newChange.setNewValue(change.getNewValue());
                newChange.setChangeType(configChangeType(change.getChangeType()));
                changeMap.put(key, newChange);
            }
        }

        newEvent.setNamespace(event.getNamespace());
        newEvent.setChanges(changeMap);

        return newEvent;
    }

    /**
     * 枚举类型转换
     *
     * @param changeType
     * @return
     */
    private BaseApolloConfigChangeType configChangeType(PropertyChangeType changeType) {
        switch (changeType) {
            case ADDED:
                return BaseApolloConfigChangeType.ADD;
            case MODIFIED:
                return BaseApolloConfigChangeType.MODIFY;
            case DELETED:
                return BaseApolloConfigChangeType.DELETE;
            default:
                return null;
        }
    }
}
