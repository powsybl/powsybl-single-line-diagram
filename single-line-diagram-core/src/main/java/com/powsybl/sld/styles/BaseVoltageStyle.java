/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.styles;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class BaseVoltageStyle {

    private static final String CONFIG_FILE = "base-voltages.yml";

    private final BaseVoltagesConfig config;

    protected BaseVoltageStyle(BaseVoltagesConfig config) {
        this.config = Objects.requireNonNull(config);
    }

    public static BaseVoltageStyle fromPlatformConfig() {
        return fromPath(PlatformConfig.defaultConfig().getConfigDir().resolve(CONFIG_FILE));
    }

    public static BaseVoltageStyle fromInputStream(InputStream configInputStream) {
        Objects.requireNonNull(configInputStream);
        Yaml yaml = new Yaml(new Constructor(BaseVoltagesConfig.class));
        BaseVoltagesConfig config = yaml.load(configInputStream);
        return new BaseVoltageStyle(config);
    }

    public static BaseVoltageStyle fromPath(Path configFile) {
        Objects.requireNonNull(configFile);
        if (Files.exists(configFile)) {
            try (InputStream configInputStream = Files.newInputStream(configFile)) {
                return fromInputStream(configInputStream);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            InputStream configInputStream = BaseVoltageStyle.class.getResourceAsStream("/" + CONFIG_FILE);
            if (configInputStream != null) {
                return fromInputStream(configInputStream);
            } else {
                throw new PowsyblException("No base voltages configuration found");
            }
        }
    }

    public List<String> getProfiles() {
        return config.getBaseVoltages()
                     .stream()
                     .map(BaseVoltageConfig::getProfile)
                     .distinct()
                     .collect(Collectors.toList());
    }

    public String getDefaultProfile() {
        return config.getDefaultProfile();
    }

    public List<String> getBaseVoltageNames(String profile) {
        Objects.requireNonNull(profile);
        return config.getBaseVoltages()
                     .stream()
                     .filter(baseVoltage -> baseVoltage.getProfile().equals(profile))
                     .map(BaseVoltageConfig::getName)
                     .collect(Collectors.toList());
    }

    public Optional<String> getBaseVoltageName(double baseVoltage, String profile) {
        Objects.requireNonNull(profile);
        return config.getBaseVoltages()
                     .stream()
                     .filter(v -> v.getProfile().equals(profile)
                                            && v.getMinValue() <= baseVoltage
                                            && v.getMaxValue() > baseVoltage)
                     .map(BaseVoltageConfig::getName)
                     .findFirst();
    }

}
