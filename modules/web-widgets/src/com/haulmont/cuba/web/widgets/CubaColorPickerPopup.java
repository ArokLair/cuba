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

package com.haulmont.cuba.web.widgets;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.shared.ui.colorpicker.Color;
import com.vaadin.ui.components.colorpicker.ColorPickerPopup;

public class CubaColorPickerPopup extends ColorPickerPopup {

    public CubaColorPickerPopup(Color initialColor) {
        super(initialColor);
    }

    @Override
    protected VerticalLayout createHistoryOuterContainer(VerticalLayout innerContainer) {
        innerContainer.setMargin(false);
        VerticalLayout historyOuterContainer = super.createHistoryOuterContainer(innerContainer);
        historyOuterContainer.setMargin(new MarginInfo(false, true, false, true));
        return historyOuterContainer;
    }

    public void setConfirmButtonCaption(String caption) {
        ok.setCaption(caption);
    }

    public void setCancelButtonCaption(String caption) {
        cancel.setCaption(caption);
    }

    public void setSwatchesTabCaption(String caption) {
        ((TabSheet) swatchesTab.getParent()).getTab(swatchesTab).setCaption(caption);
    }

    public void setLookupAllCaption(String caption) {
        ((CubaColorPickerSelect) colorSelect).setAllCaption(caption);
    }

    public void setLookupRedCaption(String caption) {
        ((CubaColorPickerSelect) colorSelect).setRedCaption(caption);
    }

    public void setLookupGreenCaption(String caption) {
        ((CubaColorPickerSelect) colorSelect).setGreenCaption(caption);
    }

    public void setLookupBlueCaption(String caption) {
        ((CubaColorPickerSelect) colorSelect).setBlueCaption(caption);
    }

    @Override
    protected Component createSelectTab() {
        VerticalLayout selLayout = new VerticalLayout();
        selLayout.setSpacing(false);
        selLayout.setMargin(new MarginInfo(false, false, true, false));
        selLayout.addComponent(selPreview);
        selLayout.addStyleName("seltab");

        colorSelect = new CubaColorPickerSelect();
        colorSelect.addValueChangeListener(this::colorChanged);

        selLayout.addComponent(colorSelect);
        return selLayout;
    }
}