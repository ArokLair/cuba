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

package spec.cuba.web

import com.haulmont.cuba.core.app.ConfigStorageService
import com.haulmont.cuba.core.global.*
import com.haulmont.cuba.gui.model.DataContextFactory
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory
import com.haulmont.cuba.web.App
import com.haulmont.cuba.web.DefaultApp
import com.haulmont.cuba.web.testsupport.TestContainer
import com.haulmont.cuba.web.testsupport.TestServiceProxy
import com.vaadin.server.VaadinSession
import com.vaadin.ui.ConnectorTracker
import com.vaadin.ui.UI
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

class WebSpec extends Specification {

    @Shared @ClassRule
    public TestContainer cont = TestContainer.Common.INSTANCE

    protected Metadata metadata
    protected MetadataTools metadataTools
    protected ViewRepository viewRepository
    protected EntityStates entityStates
    protected DataManager dataManager
    protected DataContextFactory dataContextFactory
    protected ComponentsFactory componentsFactory

    void setup() {
        metadata = cont.getBean(Metadata)
        metadataTools = cont.getBean(MetadataTools)
        viewRepository = cont.getBean(ViewRepository)
        entityStates = cont.getBean(EntityStates)
        dataManager = cont.getBean(DataManager)
        dataContextFactory = cont.getBean(DataContextFactory)
        componentsFactory = cont.getBean(ComponentsFactory)

        // all the rest is required for web components

        TestServiceProxy.mock(ConfigStorageService, Mock(ConfigStorageService) {
            getDbProperties() >> [:]
        })

        App app = new DefaultApp()

        VaadinSession vaadinSession = Mock() {
            hasLock() >> true
            getAttribute(App) >> app
        }
        VaadinSession.setCurrent(vaadinSession)

        ConnectorTracker vaadinConnectorTracker = Mock() {
            isWritingResponse() >> false
        }
        UI vaadinUi = Mock() {
            getConnectorTracker() >> vaadinConnectorTracker
        }
        UI.setCurrent(vaadinUi)
    }

    void cleanup() {
        TestServiceProxy.clear()
    }
}
