/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.data;

/**
 * Immutable data object for global configuration property.
 */
public class GlobalConfigurationPropertyData {

    @SuppressWarnings("unused")
    private String name;
    private String value;
    @SuppressWarnings("unused")
    private boolean enabled;
    private Long id;

    public GlobalConfigurationPropertyData(Long id, final String name, final boolean enabled,final String value) {
        this.id=id;
    	this.name = name;
        this.enabled = enabled;
        this.value=value;
    }
}