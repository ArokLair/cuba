/*
 * Copyright (c) 2008-2018 Haulmont.
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

package com.haulmont.cuba.web.widgets.client.timefield;

import com.haulmont.cuba.web.widgets.client.textfield.CubaMaskedFieldWidget;

public class CubaTimeFieldWidget extends CubaMaskedFieldWidget {

    public static final String CLASSNAME = "c-timefield";

    protected static final String EMPTY_FIELD_CLASSNAME = "c-timefield-empty";

    @Override
    public void valueChange(boolean blurred) {
        String newText = getValue();

        if (!newText.equals(valueBeforeEdit)) {
            newText = (newText.endsWith("__") && !newText.startsWith("__"))
                    ? newText.replaceAll("__", "00")
                    : newText;

            if (validateText(newText)) {
                valueBeforeEdit = newText;
                setValue(newText);
            } else {
                setValue(valueBeforeEdit);
            }
        }
    }

    @Override
    protected String getEmptyFieldClass() {
        return EMPTY_FIELD_CLASSNAME;
    }
}
