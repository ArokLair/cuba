/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */
package com.haulmont.cuba.gui.app.security.user;

import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.components.FieldGroup;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.security.global.UserUtils;
import org.apache.commons.lang.StringUtils;

import java.text.ParseException;

public class NameBuilderListener<T extends Entity> implements Datasource.ItemPropertyChangeListener<T> {

    public static final String DEFAULT_NAME_PATTERN = "{FF| }{LL}";

    protected Window window;
    protected FieldGroup fieldGroup;
    protected Datasource datasource;

    protected String pattern;

    protected NameBuilderListener() {
    }

    public NameBuilderListener(Window window) {
        if (window == null)
            throw new IllegalArgumentException("window is null");

        this.window = window;
    }

    public NameBuilderListener(Window window, String pattern) {
        if (window == null)
            throw new IllegalArgumentException("window is null");

        this.window = window;
        this.pattern = pattern;
    }

    public NameBuilderListener(FieldGroup fieldGroup) {
        if (fieldGroup == null)
            throw new IllegalArgumentException("fieldGroup is null");

        this.fieldGroup = fieldGroup;
    }

    public NameBuilderListener(Datasource datasource) {
        this.datasource = datasource;
    }

    protected boolean isGeneratingDisplayName(
            String pattern, String firstName, String lastName, String middleName, String property, Object prevValue) {
        String name = getFieldValue("name");
        if (StringUtils.isNotEmpty(name)) {

            switch (property) {
                case "firstName": firstName = (String) prevValue;
                    break;
                case "lastName": lastName = (String) prevValue;
                    break;
                case "middleName": middleName = (String) prevValue;
                    break;
            }

            String displayName;
            try {
                displayName = UserUtils.formatName(pattern, firstName, lastName, middleName);
            } catch (ParseException e) {
                return false;
            }

            if (!name.equals(displayName)) {
                return true;
            }
        }

        return false;
    }

    protected void setFullName(String displayedName) {
        if (datasource != null) {
            datasource.getItem().setValue("name", displayedName);
        } else if (window != null) {
            Field field = (Field) window.getComponentNN("name");
            field.setValue(displayedName);
        } else {
            fieldGroup.setFieldValue("name", displayedName);
        }
    }

    protected String getFieldValue(String name) {
        if (datasource != null) {
            return datasource.getItem().getValue(name);
        } else if (window != null) {
            Field field = (Field) window.getComponentNN(name);
            return (String) field.getValue();
        } else {
            return (String) fieldGroup.getFieldValue(name);
        }
    }

    @Override
    public void itemPropertyChanged(Datasource.ItemPropertyChangeEvent<T> e) {

        if (!"firstName".equals(e.getProperty())
                && !"lastName".equals(e.getProperty())
                && !"middleName".equals(e.getProperty())) {
            return;
        }

        String firstName = getFieldValue("firstName");
        String lastName = getFieldValue("lastName");
        String middleName = getFieldValue("middleName");

        String displayedName;
        try {
            if (this.pattern == null) {
                pattern = AppContext.getProperty("cuba.user.fullNamePattern");
                if (StringUtils.isBlank(pattern))
                    pattern = DEFAULT_NAME_PATTERN;
            }

            if (isGeneratingDisplayName(pattern, firstName, lastName, middleName, e.getProperty(), e.getPrevValue())) {
                return;
            }

            displayedName = UserUtils.formatName(pattern, firstName, lastName, middleName);
        } catch (ParseException pe) {
            displayedName = "";
        }

        MetaProperty nameProperty = e.getItem().getMetaClass().getProperty("name");
        if (nameProperty != null && nameProperty.getAnnotations().containsKey("length")) {
            int length = (int) nameProperty.getAnnotations().get("length");
            if (displayedName.length() > length) {
                displayedName = "";
            }
        }

        setFullName(displayedName);
    }
}