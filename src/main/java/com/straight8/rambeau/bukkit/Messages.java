package com.straight8.rambeau.bukkit;

import dev.ratas.slimedogcore.api.config.SDCCustomConfig;
import dev.ratas.slimedogcore.api.messaging.factory.SDCDoubleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.factory.SDCSingleContextMessageFactory;
import dev.ratas.slimedogcore.impl.messaging.MessagesBase;
import dev.ratas.slimedogcore.impl.messaging.factory.MsgUtil;

public class Messages extends MessagesBase {
    private SDCSingleContextMessageFactory<Integer> pageHeader;
    private SDCDoubleContextMessageFactory<String, String> enabledVersion;
    private SDCDoubleContextMessageFactory<String, String> disabledVersion;

    protected Messages(SDCCustomConfig config) {
        super(config);
        load();
    }

    private void load() {
        pageHeader = MsgUtil.singleContext("{page}", page -> String.valueOf(page),
                getRawMessage("page-header-format", "PluginVersions ===== page {page} ====="));
        enabledVersion = MsgUtil.doubleContext("{name}", name -> name, "{version}",
                version -> version,
                getRawMessage("enabled-version-format", " - &a{name} &e{version}"));
        disabledVersion = MsgUtil.doubleContext("{name}", name -> name, "{version}",
                version -> version,
                getRawMessage("disabled-version-format", " - &c{name} &e{version}"));
    }

    public SDCSingleContextMessageFactory<Integer> getPageHeader() {
        return pageHeader;
    }

    public SDCDoubleContextMessageFactory<String, String> getEnabledVersion() {
        return enabledVersion;
    }

    public SDCDoubleContextMessageFactory<String, String> getDisabledVersion() {
        return disabledVersion;
    }

}
