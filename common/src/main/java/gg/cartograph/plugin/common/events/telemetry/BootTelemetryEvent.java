package gg.cartograph.plugin.common.events.telemetry;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import gg.cartograph.plugin.common.NodeType;
import gg.cartograph.plugin.common.events.*;

import java.util.List;

/**
 * Telemetry event fired once when the Cartograph plugin enables on a node.
 *
 * <p>Carries the stable-until-restart snapshot of the node: server software,
 * version info, runtime environment, configuration, installed plugins, and
 * worlds.</p>
 *
 * <p>On proxy nodes ({@link NodeType#PROXY}), {@code minecraftVersion} and
 * {@code worlds} are {@code null} and excluded from the serialised payload.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BootTelemetryEvent(
        @JsonGetter("ts") Long timestamp,
        @JsonGetter("sw") String software,
        @JsonGetter("sv") String softwareVersion,
        @JsonGetter("mv") String minecraftVersion,
        @JsonGetter("jv") String javaVersion,
        @JsonGetter("jr") String javaVendor,
        @JsonGetter("os") OsInfo os,
        @JsonGetter("pv") String pluginVersion,
        @JsonGetter("nt") NodeType nodeType,
        @JsonGetter("mp") Integer maxPlayers,
        @JsonGetter("vd") Integer viewDistance,
        @JsonGetter("sd") Integer simulationDistance,
        @JsonGetter("om") Boolean onlineMode,
        @JsonGetter("wl") Boolean whitelist,
        @JsonGetter("mo") String motd,
        @JsonGetter("pl") List<PluginInfo> plugins,
        @JsonGetter("bk") List<BackendInfo> backends,
        @JsonGetter("w") List<WorldInfo> worlds,
        @JsonGetter("rp") List<ResourcePackInfo> resourcePacks,
        @JsonGetter("md") List<ModInfo> mods,
        @JsonGetter("cv") ClientVersionInfo clientVersions,
        @JsonGetter("br") BedrockSupportInfo bedrockSupport
) implements TelemetryEvent
{
    @Override
    @JsonGetter("t")
    public String type()
    {
        return EventTypes.BOOT;
    }
}