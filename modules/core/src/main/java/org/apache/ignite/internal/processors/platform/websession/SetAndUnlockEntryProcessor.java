/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.processors.platform.websession;

import org.apache.ignite.cache.CacheEntryProcessor;
import org.apache.ignite.internal.util.typedef.internal.S;

import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;

/**
 * Entry processor that unlocks web session data.
 */
public class SetAndUnlockEntryProcessor implements CacheEntryProcessor<String, SessionStateData, Object> {
    /** */
    private static final long serialVersionUID = 0L;

    /** {@inheritDoc} */
    @Override public Object process(MutableEntry<String, SessionStateData> entry, Object... args)
        throws EntryProcessorException {
        assert entry.exists();

        SessionStateData data = entry.getValue();

        assert data != null;
        assert data.lockNodeId() != null;

        SessionStateData newData = (SessionStateData)args[0];

        if (!data.lockNodeId().equals(newData.lockNodeId()))
            throw new IllegalStateException("Can not unlock session data: lock node id check failed.");

        if (data.lockId() != newData.lockId())
            throw new IllegalStateException("Can not unlock session data: lock id check failed.");

        // Unlock.
        data.unlock();

        // Update values.
        data.applyChanges(newData);

        // Apply.
        entry.setValue(data);

        return null;
    }

    /** {@inheritDoc */
    @Override public String toString() {
        return S.toString(SetAndUnlockEntryProcessor.class, this);
    }
}
