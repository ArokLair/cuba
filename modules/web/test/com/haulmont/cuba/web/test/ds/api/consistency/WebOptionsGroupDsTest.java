/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.cuba.web.test.ds.api.consistency;

import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.gui.components.ds.api.consistency.OptionsGroupDsTest;
import com.haulmont.cuba.web.gui.WebComponentsFactory;
import mockit.Expectations;

public class WebOptionsGroupDsTest extends OptionsGroupDsTest {

    public WebOptionsGroupDsTest() {
        factory = new WebComponentsFactory();
    }

    @Override
    protected void initExpectations() {
        super.initExpectations();

        new Expectations() {
            {
                AppContext.getProperty("cuba.mainMessagePack"); result = "com.haulmont.cuba.web"; minTimes = 0;
            }
        };
    }
}
