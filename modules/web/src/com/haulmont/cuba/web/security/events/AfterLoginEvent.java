/*
 * Copyright (c) 2008-2017 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.cuba.web.security.events;

import com.haulmont.cuba.security.auth.AuthenticationDetails;
import com.haulmont.cuba.security.auth.Credentials;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.Connection;
import org.springframework.context.ApplicationEvent;

import javax.annotation.Nullable;

/**
 * Event that is fired after login procedure in {@link Connection}.
 */
public class AfterLoginEvent extends ApplicationEvent {

    protected final AuthenticationDetails authenticationDetails;

    public AfterLoginEvent(Credentials source, @Nullable AuthenticationDetails authenticationDetails) {
        super(source);
        this.authenticationDetails = authenticationDetails;
    }

    @Override
    public Credentials getSource() {
        return (Credentials) super.getSource();
    }

    public Credentials getCredentials() {
        return (Credentials) super.getSource();
    }

    @Nullable
    public UserSession getUserSession() {
        return authenticationDetails != null ? authenticationDetails.getSession() : null;
    }

    @Nullable
    public AuthenticationDetails getAuthenticationDetails() {
        return authenticationDetails;
    }
}