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

package spec.cuba.web.datacontext

import com.haulmont.cuba.core.app.DataService
import com.haulmont.cuba.core.app.serialization.EntitySerializationAPI
import com.haulmont.cuba.core.entity.Entity
import com.haulmont.cuba.core.global.CommitContext
import com.haulmont.cuba.gui.model.CollectionContainer
import com.haulmont.cuba.gui.model.DataContext
import com.haulmont.cuba.gui.model.InstanceContainer
import com.haulmont.cuba.gui.model.InstanceLoader
import com.haulmont.cuba.web.testmodel.sales.Customer
import com.haulmont.cuba.web.testmodel.sales.Order
import com.haulmont.cuba.web.testmodel.sales.OrderLine
import com.haulmont.cuba.web.testmodel.sales.Product
import com.haulmont.cuba.web.testsupport.TestServiceProxy
import spec.cuba.web.WebSpec

import static com.haulmont.cuba.client.testsupport.TestSupport.reserialize

class CompositionTest extends WebSpec {

    private Customer customer1
    private Order order1
    private Product product11, product12
    private OrderLine orderLine11, orderLine12

    class OrderScreen {
        DataContext dataContext
        InstanceContainer<Order> orderCnt
        CollectionContainer<OrderLine> orderLinesCnt

        def open(Order order) {
            dataContext = dataContextFactory.createDataContext()
            orderCnt = dataContextFactory.createInstanceContainer(Order)
            orderLinesCnt = dataContextFactory.createCollectionContainer(OrderLine)
            orderCnt.addItemChangeListener { e ->
                orderLinesCnt.setItems(e.item.orderLines)
            }

            InstanceLoader orderLdr = dataContextFactory.createInstanceLoader()
            orderLdr.setContainer(orderCnt)
            orderLdr.setDataContext(dataContext)

            orderLdr.entityId = order.id
            orderLdr.load()
        }

        def commit() {
            dataContext.commit()
        }
    }

    class OrderLineScreen {
        DataContext dataContext
        InstanceContainer<OrderLine> orderLineCnt

        def open(OrderLine orderLine, DataContext parentContext) {
            dataContext = dataContextFactory.createDataContext()
            if (parentContext != null)
                dataContext.setParent(parentContext)
            orderLineCnt = dataContextFactory.createInstanceContainer(OrderLine)

            if (!dataContext.contains(orderLine)) {
                InstanceLoader loader = dataContextFactory.createInstanceLoader()
                loader.setContainer(orderLineCnt)
                loader.setDataContext(dataContext)
                loader.setEntityId(orderLine.id)
                loader.load()
            } else {
                orderLineCnt.item = dataContext.find(OrderLine, orderLine.id)
            }
        }

        def commit() {
            dataContext.commit()
        }
    }

    class ProductScreen {
        DataContext dataContext
        InstanceContainer<Product> productCnt

        def open(Product product, DataContext parentContext) {
            dataContext = dataContextFactory.createDataContext()
            if (parentContext != null)
                dataContext.setParent(parentContext)
            productCnt = dataContextFactory.createInstanceContainer(Product)

            if (!dataContext.contains(product)) {
                InstanceLoader loader = dataContextFactory.createInstanceLoader()
                loader.setContainer(productCnt)
                loader.setDataContext(dataContext)
                loader.setEntityId(product.id)
                loader.load()
            } else {
                productCnt.item = dataContext.find(Product, product.id)
            }
        }

        def commit() {
            dataContext.commit()
        }
    }

    @Override
    void setup() {
        customer1 = makeSaved(new Customer(name: "customer-1"))

        order1 = makeSaved(new Order(number: "111", orderLines: []))
        order1.customer = customer1

        product11 = makeSaved(new Product(name: "product-11", price: 100))

        product12 = makeSaved(new Product(name: "product-12", price: 200))

        orderLine11 = makeSaved(new OrderLine(quantity: 10))
        orderLine11.order = order1
        orderLine11.product = product11

        orderLine12 = makeSaved(new OrderLine(quantity: 20))
        orderLine12.order = order1
        orderLine12.product = product11

        order1.orderLines.addAll([orderLine11, orderLine12])
    }

    private mockLoad(Entity loadedEntity) {
        TestServiceProxy.mock(DataService, Mock(DataService) {
            load(_) >> reserialize(loadedEntity)
        })
    }

    private List mockCommit() {
        def updated = []
        TestServiceProxy.mock(DataService, Mock(DataService) {
            commit(_) >> { CommitContext cc ->
                println ">>> committing $cc.commitInstances"
                updated.addAll(cc.commitInstances)
                TestServiceProxy.getDefault(DataService).commit(cc)
            }
        })
        updated
    }

    private static <T> T makeSaved(T entity) {
        TestServiceProxy.getDefault(DataService).commit(new CommitContext().addInstanceToCommit(entity))[0] as T
    }

    def "zero composition"() {

        def orderScreen = new OrderScreen()

        when:

        mockLoad(order1)
        orderScreen.open(order1)

        then:

        orderScreen.orderCnt.item == order1

        when:

        orderScreen.orderCnt.item.number = '222'
        List updated = mockCommit()

        orderScreen.commit()

        then:

        updated.size() == 1
    }

    def "one level of composition"() {

        EntitySerializationAPI entitySerialization = cont.getBean(EntitySerializationAPI.NAME, EntitySerializationAPI)

        def orderScreen = new OrderScreen()
        def orderLineScreen = new OrderLineScreen()

        when:

        mockLoad(order1)
        orderScreen.open(order1)

        then:

        orderScreen.orderCnt.item == order1
        orderScreen.orderLinesCnt.items.size() == 2
        orderScreen.orderLinesCnt.item == null

        when: "open edit screen for orderLine11"

        orderScreen.orderLinesCnt.item = orderLine11
        mockLoad(orderScreen.orderLinesCnt.item)
        orderLineScreen.open(orderScreen.orderLinesCnt.item, orderScreen.dataContext)

        then:

        !orderScreen.dataContext.hasChanges()
        orderLineScreen.orderLineCnt.item == orderLine11
        orderLineScreen.orderLineCnt.item.quantity == 10

        when: "change orderLine11.quantity and commit child context"

        orderLineScreen.orderLineCnt.item.quantity = 11

        def childContextStateBeforeCommit = entitySerialization.toJson(orderLineScreen.dataContext.getAll())

        def updated = mockCommit()
        def modified = []
        orderLineScreen.dataContext.addPreCommitListener { e ->
            modified.addAll(e.modifiedInstances)
        }
        orderLineScreen.commit()

        def childContextStateAfterCommit = entitySerialization.toJson(orderLineScreen.dataContext.getAll())

        then: "child context committed orderLine11 to parent"

        modified.contains(orderLine11)
        updated.isEmpty()
        orderScreen.orderLinesCnt.item.quantity == 11

        and: "child context has no changes anymore"

        !orderLineScreen.dataContext.hasChanges()

        and: "child context is exactly the same as before commit"

        childContextStateAfterCommit == childContextStateBeforeCommit

        when: "commit parent context"

        orderScreen.commit()

        then: "orderLine11 committed to DataService"

        updated.size() == 1
        updated.contains(orderLine11)
        updated.find { it == orderLine11 }.quantity == 11
    }

    def "two levels of composition"() {

        def orderScreen = new OrderScreen()
        def orderLineScreen = new OrderLineScreen()
        def productScreen = new ProductScreen()

        when:

        mockLoad(order1)
        orderScreen.open(order1)

        orderScreen.orderLinesCnt.item = orderLine11
        mockLoad(orderScreen.orderLinesCnt.item)
        orderLineScreen.open(orderScreen.orderLinesCnt.item, orderScreen.dataContext)

        mockLoad(orderScreen.orderLinesCnt.item.product)
        productScreen.open(orderScreen.orderLinesCnt.item.product, orderLineScreen.dataContext)

        then:

        productScreen.productCnt.item == product11

        when:

        productScreen.productCnt.item.price = 101

        def updated = mockCommit()
        productScreen.commit()

        then:

        updated.isEmpty()

        when:

        orderLineScreen.orderLineCnt.item.quantity = 11
        orderLineScreen.commit()

        then:

        updated.isEmpty()

        when:

        orderScreen.commit()

        then:

        updated.size() == 2
        updated.contains(product11)
        updated.contains(orderLine11)
        updated.find { it == product11}.price == 101
        updated.find { it == orderLine11}.quantity == 11
    }

    def "one level of composition - repetitive edit"() {

        def orderScreen = new OrderScreen()
        def orderLineScreen = new OrderLineScreen()

        mockLoad(order1)
        orderScreen.open(order1)

        orderScreen.orderLinesCnt.item = orderLine11
        mockLoad(orderLine11)
        orderLineScreen.open(orderScreen.orderLinesCnt.item, orderScreen.dataContext)
        orderLineScreen.orderLineCnt.item.quantity = 11
        orderLineScreen.commit()

        when: "open orderLineScreen second time"

        mockLoad(orderLine11)
        orderLineScreen.open(orderScreen.orderLinesCnt.item, orderScreen.dataContext)

        then:

        orderLineScreen.orderLineCnt.item.quantity == 11

        when:

        orderLineScreen.orderLineCnt.item.quantity = 12

        def updated = mockCommit()
        orderLineScreen.commit()
        orderScreen.commit()

        then:

        updated.size() == 1
        updated.find { it == orderLine11}.quantity == 12

    }
}
