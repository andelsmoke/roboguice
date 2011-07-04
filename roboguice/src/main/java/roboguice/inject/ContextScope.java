/*
 * Copyright 2009 Michael Burton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package roboguice.inject;

import android.app.Application;
import android.content.Context;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Burton
 */
public class ContextScope implements Scope {

    protected HashMap<Context, Map<Key<?>, Object>> values = new HashMap<Context, Map<Key<?>, Object>>();
    protected ThreadLocal<Context> contextThreadLocal = new ThreadLocal<Context>();


    public ContextScope(Application app) {
        open(app);
    }


    public void open(Context context) {

        final Context prevContext = contextThreadLocal.get();
        if( prevContext==context )
            return;

        
        
        // Mark this thread as for this context
        contextThreadLocal.set(context);

        // Add the context to the scope
        getScopedObjectMap(context).put(Key.get(Context.class), context);

    }

    public void close( Context context ) {

        final Context prevContext = contextThreadLocal.get();
        if( prevContext==context )
            contextThreadLocal.set(null);

        values.remove(context);
    }


    public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
        return new Provider<T>() {
            public T get() {
                final Context context = contextThreadLocal.get();
                if (context != null) {
                    final Map<Key<?>, Object> scopedObjects = getScopedObjectMap(context);

                    @SuppressWarnings({"unchecked"}) T current = (T) scopedObjects.get(key);

                    if (current == null && !scopedObjects.containsKey(key)) {
                        current = unscoped.get();
                        scopedObjects.put(key, current);
                    }
                    return current;
                }
                
                throw new UnsupportedOperationException("Can't perform injection outside of a context scope");
            }
        };

    }

    protected Map<Key<?>, Object> getScopedObjectMap(Context context) {

        Map<Key<?>, Object> scopedObjects = values.get(context);
        if (scopedObjects == null) {
            scopedObjects = new HashMap<Key<?>, Object>();
            values.put(context, scopedObjects);
        }
        return scopedObjects;
    }

}
