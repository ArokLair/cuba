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

package com.haulmont.cuba.core.app;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.EntitySnapshot;
import com.haulmont.cuba.core.entity.diff.EntityDiff;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.security.entity.User;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Creates and analyzes entity snapshots.
 */
public interface EntitySnapshotAPI {

    String NAME = "cuba_EntitySnapshotManager";

    /**
     * Load snapshots for entity
     *
     * @param metaClass Entity metaclass
     * @param id        Entity Id
     * @return Snapshot list sorted by snapshotDate desc
     */
    List<EntitySnapshot> getSnapshots(MetaClass metaClass, Object id);

    /**
     * Translate snapshots for archival classes
     *
     * @param metaClass    Metaclass
     * @param id           Entity Id
     * @param classMapping Map of [OldClass -&gt; NewClass] for migration
     */
    void migrateSnapshots(MetaClass metaClass, Object id, Map<Class, Class> classMapping);

    /**
     * Create snapshot for Entity and store it to database
     *
     * @param entity Entity
     * @param view   View
     * @return Snapshot
     */
    EntitySnapshot createSnapshot(Entity entity, View view);

    /**
     * Create snapshot for Entity with specific date and store it to database
     *
     * @param entity       Entity
     * @param view         View
     * @param snapshotDate Date
     * @return Snapshot
     */
    EntitySnapshot createSnapshot(Entity entity, View view, Date snapshotDate);

    /**
     * Create snapshot for Entity with specific date and author and store it to database
     *
     * @param entity       Entity
     * @param view         View
     * @param snapshotDate Date
     * @param author       Author
     * @return Snapshot
     */
    EntitySnapshot createSnapshot(Entity entity, View view, Date snapshotDate, User author);

    /**
     * Restore entity by snapshot
     *
     * @param snapshot Snapshot
     * @return Entity instance
     */
    Entity extractEntity(EntitySnapshot snapshot);

    /**
     * Restore view from snapshot
     *
     * @param snapshot Snapshot
     * @return View instance
     */
    View extractView(EntitySnapshot snapshot);

    /**
     * Diff two versions of entity
     *
     * @param first  First version
     * @param second Second version
     * @return Diffs
     */
    EntityDiff getDifference(@Nullable EntitySnapshot first, EntitySnapshot second);

    /**
     * Gets last added snapshot to the database for given entity.
     *
     * @param entity entity
     * @return snapshot or null if there is no snapshots in database for the given entity
     */
    @Nullable
    EntitySnapshot getLastEntitySnapshot(Entity entity);

    /**
     * Gets last added snapshot to the database for entity by id.
     *
     * @param metaClass entity meta class
     * @param id        entity id
     * @return snapshot or null if there is no snapshots in database for the given entity
     */
    @Nullable
    EntitySnapshot getLastEntitySnapshot(MetaClass metaClass, Object id);

    /**
     * Creates not persistence snapshot for entity.
     *
     * @param entity entity
     * @param view   view
     * @return not persistence snapshot
     */
    EntitySnapshot createTempSnapshot(Entity entity, View view);

    /**
     * Creates not persistence snapshot for entity with a specific date.
     *
     * @param entity       entity
     * @param view         entity view
     * @param snapshotDate date
     * @return not persistence snapshot
     */
    EntitySnapshot createTempSnapshot(Entity entity, View view, Date snapshotDate);

    /**
     * Creates not persistence snapshot for Entity with a specific date and author.
     *
     * @param entity       entity
     * @param view         entity view
     * @param snapshotDate date
     * @param author       author
     * @return not persistence snapshot
     */
    EntitySnapshot createTempSnapshot(Entity entity, View view, Date snapshotDate, User author);
}