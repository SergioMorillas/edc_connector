/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.vault.hashicorp.model;

import java.util.Map.Entry;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

public class GetEntryResponsePayload {

    private GetEntryResponsePayloadGetVaultEntryData data;

    private GetEntryResponsePayload() {}

    public GetEntryResponsePayloadGetVaultEntryData getData() {
        return this.data;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        private final GetEntryResponsePayload getEntryResponsePayload;

        private Builder() {
            getEntryResponsePayload = new GetEntryResponsePayload();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder data(GetEntryResponsePayloadGetVaultEntryData data) {
            for(Entry<String, String> innerEntry : data.getData().entrySet()){
                String key    = innerEntry.getKey();
                String value  = innerEntry.getValue();
                System.out.println("Clave: " + key);
                System.out.println("Valor: " + value);
            }
            getEntryResponsePayload.data = data;
            return this;
        }

        public GetEntryResponsePayload build() {
            for(Entry<String, String> innerEntry : getEntryResponsePayload.getData().getData().entrySet()){
                String key    = innerEntry.getKey();
                String value  = innerEntry.getValue();
                System.out.println("Clave: " + key);
                System.out.println("Valor: " + value);
            }
            return getEntryResponsePayload;
        }
    }
}
