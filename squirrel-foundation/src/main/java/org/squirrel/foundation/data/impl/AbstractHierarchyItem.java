package org.squirrel.foundation.data.impl;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.squirrel.foundation.component.SquirrelProvider;
import org.squirrel.foundation.data.HierarchyItem;
import org.squirrel.foundation.data.ItemProvider;
import org.squirrel.foundation.event.EventMediator;
import org.squirrel.foundation.exception.ErrorCodes;
import org.squirrel.foundation.exception.SquirrelRuntimeException;
import org.squirrel.foundation.util.ReflectUtils;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * The generic implementation of tree structure with change notifications.
 * 
 * @author Henry.He
 * 
 * @param <M>
 *            the type of tree item
 * @param <N>
 *            the type of item value
 */
public abstract class AbstractHierarchyItem<M extends HierarchyItem<M, N>, N>
        implements HierarchyItem<M, N> {

    private AbstractHierarchyItem<M, N> parent;

    private List<M> children;

    private N value;

    private Class<?> type;

    protected boolean isLeaf;

    private Map<String, Object> data;

    private boolean notifiable = true;

    private EventMediator eventMediator;

    public AbstractHierarchyItem(M parent, Class<?> type, boolean isLeaf) {
        setParent(parent);
        this.type = type;
        this.isLeaf = isLeaf;
    }

    @SuppressWarnings("unchecked")
    @Override
    public M getParent() {
        return (M) parent;
    }

    @Override
    public N getValue() {
        return value;
    }

    @Override
    public void setValue(N newValue) {
        if (newValue != value) {
            N oldValue = value;
            value = newValue;
            fireEvent(new ValueChangeEventImpl<M, N>(getCurrent(), oldValue,
                    newValue));
        }
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public M detach() {
        M oldParent = getParent();
        setParent(null);
        return oldParent;
    }

    protected void setParent(M newParent) {
        Preconditions.checkArgument(newParent == null
                || newParent instanceof AbstractHierarchyItem);
        if (parent != null) {
            parent.removeChild(getCurrent());
        }
        parent = (AbstractHierarchyItem<M, N>) newParent;
        if (parent != null) {
            parent.addChild(getCurrent());
        }
    }

    @SuppressWarnings("unchecked")
    protected M getCurrent() {
        return (M) this;
    }

    private void addChild(M child) {
        // always make sure the child items was retrieved before new child added
        // in.
        if (children == null) { // if(getChildren().isEmpty())
            children = Lists.newArrayList();
        }
        isLeaf = false;
        children.add(child);
        fireEvent(new ItemAttachedEventImpl<M, N>(getCurrent(), child));
    }

    private void removeChild(M child) {
        if (children == null)
            return;
        children.remove(child);
        if (children.isEmpty())
            children = null;
        fireEvent(new ItemRemovedEventImpl<M, N>(getCurrent(), child));
    }

    @Override
    public List<M> getChildren() {
        if (isLeaf) {
            return Collections.emptyList();
        }

        if (children == null) {
            getItemProviderInternal().createChildren(getCurrent());
            if (children == null) {
                // outline provider did not create any child
                isLeaf = true;
                return Collections.emptyList();
            }
        }
        return Lists.newArrayList(children);
    }

    @Override
    public List<M> getRawChildren() {
        if (children == null) {
            return Collections.emptyList();
        }
        return children;
    }

    @Override
    public List<M> getAllRawChildren() {
        return getAllRawChildren(false, false);
    }

    @Override
    public List<M> getAllRawChildren(boolean includeThis, boolean isDepthFirst) {
        return getAllChildren(new Function<M, Iterable<M>>() {
            @Override
            public Iterable<M> apply(M currentItem) {
                return currentItem.getRawChildren();
            }
        }, includeThis, isDepthFirst);
    }

    protected List<M> getAllChildren(Function<M, Iterable<M>> childrenFunc,
            boolean includeThis, boolean isDepthFirst) {
        LinkedList<M> children = Lists.newLinkedList();
        Stack<M> stack = new Stack<M>();
        stack.push(getCurrent());
        while (!stack.isEmpty()) {
            M currentItem = stack.pop();
            if (isDepthFirst) {
                children.addFirst(currentItem);
            } else {
                children.addLast(currentItem);
            }
            for (M item : childrenFunc.apply(currentItem)) {
                stack.push(item);
            }
        }
        if (!includeThis) {
            children.remove(getCurrent());
        }
        return children;
    }

    @Override
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    @Override
    public boolean isRoot() {
        return parent == null;
    }

    @Override
    public M getRoot() {
        if (isRoot()) {
            return getCurrent();
        }
        return parent.getRoot();
    }

    @Override
    public List<M> getAllChildren() {
        return getAllChildren(false, false);
    }

    @Override
    public List<M> getAllChildren(boolean includeThis, boolean isDepthFirst) {
        return getAllChildren(new Function<M, Iterable<M>>() {
            @Override
            public Iterable<M> apply(M currentItem) {
                return currentItem.getChildren();
            }
        }, includeThis, isDepthFirst);
    }

    protected ItemProvider<M, N> getItemProviderInternal() {
        if (parent != null) {
            return parent.getItemProviderInternal();
        }
        throw new SquirrelRuntimeException(ErrorCodes.MISSING_ITEM_PROVIDER);
    }

    @Override
    public <T extends ItemProvider<M, N>> T getItemProvider(Class<T> type) {
        ItemProvider<M, N> itemProvider = getItemProviderInternal();
        Preconditions.checkNotNull(itemProvider,
                "The item provider cannot be null.");
        if (type.isAssignableFrom(itemProvider.getClass())) {
            return type.cast(itemProvider);
        }
        throw new SquirrelRuntimeException(ErrorCodes.ILLEGAL_TYPE_CASTING,
                itemProvider.getClass().getName(), type.getName());
    }

    @Override
    public boolean isLeaf() {
        return isLeaf;
    }

    @Override
    public void setData(String key, Object newData) {
        if (data == null) {
            data = Maps.newHashMap();
        }
        if (newData != null) {
            data.put(key, newData);
        } else {
            data.remove(key);
        }
    }

    @Override
    public Object removeData(String key) {
        if (data == null) {
            return null;
        }
        return data.remove(key);
    }

    @Override
    public Object getData(String key) {
        if (data == null) {
            return null;
        }
        return data.get(key);
    }

    public void clearData() {
        data = null;
    }

    @Override
    public boolean isNotifiable() {
        return notifiable;
    }

    @Override
    public void setNotifiable(boolean notifiable) {
        this.notifiable = notifiable;
    }

    @Override
    public void addListener(Class<?> eventType, Object listener, Method method) {
        if (eventMediator == null) {
            eventMediator = SquirrelProvider.getInstance().newInstance(EventMediator.class);
        }
        eventMediator.register(eventType, listener, method);
    }
    
    @Override
    public void addListener(Class<?> eventType, Object listener, String methodName) {
        Method method = ReflectUtils.getFirstMethodOfName(listener.getClass(), methodName);
        addListener(eventType, listener, method);
    }

    @Override
    public void removeListener(Class<?> eventType, Object listener,
            Method method) {
        if (eventMediator != null) {
            eventMediator.unregister(eventType, listener, method);
        }
    }
    
    @Override
    public void removeListener(Class<?> eventType, Object listener,
            String methodName) {
        Method method = ReflectUtils.getFirstMethodOfName(listener.getClass(), methodName);
        removeListener(eventType, listener, method);
    }

    @Override
    public void removeListener(Class<?> eventType, Object listener) {
        if (eventMediator != null) {
            eventMediator.unregister(eventType, listener);
        }
    }

    @Override
    public void removeAllListeners() {
        if (eventMediator != null)
            eventMediator.unregisterAll();
    }

    protected void fireEvent(ItemEvent<M, N> event) {
        if (eventMediator != null && isNotifiable()) {
            eventMediator.fireEvent(event);
        }
    }

    private static final Method ITEM_EVENT_METHOD = 
            ReflectUtils.getMethod(ItemEventListener.class, 
                    "itemEvent", new Class<?>[] { ItemEvent.class });
    
    @Override
    public void addListener(ItemEventListener<M, N> listener) {
        addListener(ItemEvent.class, listener, ITEM_EVENT_METHOD);
    }

    @Override
    public void removeListener(ItemEventListener<M, N> listener) {
        removeListener(ItemEvent.class, listener, ITEM_EVENT_METHOD);
    }

    // leverage bridge method to call the method of actual listener
    private static final Method VALUE_CHANGE_EVENT_METHOD = 
            ReflectUtils.getMethod(ValueChangeListener.class, "valueChange",
                    new Class<?>[] { ValueChangeEvent.class });
    
    @Override
    public void addListener(ValueChangeListener<M, N> listener) {
        addListener(ValueChangeEvent.class, listener, VALUE_CHANGE_EVENT_METHOD);
    }

    @Override
    public void removeListener(ValueChangeListener<M, N> listener) {
        removeListener(ValueChangeEvent.class, listener,
                VALUE_CHANGE_EVENT_METHOD);
    }

    private static final Method ITEM_REMOVED_EVENT_METHOD = 
            ReflectUtils.getMethod(ItemRemovedListener.class, "itemRemoved", 
                    new Class<?>[] { ItemRemovedEvent.class });
    
    @Override
    public void addListener(ItemRemovedListener<M, N> listener) {
        addListener(ItemRemovedEvent.class, listener, ITEM_REMOVED_EVENT_METHOD);
    }

    @Override
    public void removeListener(ItemRemovedListener<M, N> listener) {
        removeListener(ItemRemovedEvent.class, listener,
                ITEM_REMOVED_EVENT_METHOD);
    }

    private static final Method ITEM_ATTACHED_EVENT_METHOD = 
            ReflectUtils.getMethod(ItemAttachedListener.class, "itemAttached",
                    new Class<?>[] { ItemAttachedEvent.class });

    @Override
    public void addListener(ItemAttachedListener<M, N> listener) {
        addListener(ItemAttachedEvent.class, listener,
                ITEM_ATTACHED_EVENT_METHOD);
    }

    @Override
    public void removeListener(ItemAttachedListener<M, N> listener) {
        removeListener(ItemAttachedEvent.class, listener,
                ITEM_ATTACHED_EVENT_METHOD);
    }

    @Override
    public String toLog() {
        StringBuilder builder = new StringBuilder();
        toLog(getRoot(), "", true, builder);
        return builder.toString();
    }

    private void toLog(M parent, String prefix, boolean isTail,
            StringBuilder builder) {
        builder.append(prefix + (isTail ? "└── " : "├── ") + parent + "\n");
        if (!parent.hasChildren())
            return;
        List<M> children = parent.getChildren();
        int size = children.size();
        for (int i = 0; i < size - 1; i++) {
            toLog(children.get(i), prefix + (isTail ? "    " : "├── "), false,
                    builder);
        }
        if (size >= 1) {
            toLog(children.get(size - 1), prefix + (isTail ? "    " : "└── "),
                    true, builder);
        }
    }

    public static abstract class AbstractItemEvent<M extends HierarchyItem<M, N>, N>
            implements ItemEvent<M, N> {
        final private M source;

        public AbstractItemEvent(M source) {
            this.source = source;
        }

        @Override
        public M getSourceItem() {
            return source;
        }
    }

    public static class ValueChangeEventImpl<M extends HierarchyItem<M, N>, N>
            extends AbstractItemEvent<M, N> implements
            HierarchyItem.ValueChangeEvent<M, N> {
        final private N oldValue;
        final private N newValue;

        public ValueChangeEventImpl(M source, N oldValue, N newValue) {
            super(source);
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        @Override
        public N getOldValue() {
            return oldValue;
        }

        @Override
        public N getNewValue() {
            return newValue;
        }
    }

    public static class ItemRemovedEventImpl<M extends HierarchyItem<M, N>, N>
            extends AbstractItemEvent<M, N> implements
            HierarchyItem.ItemRemovedEvent<M, N> {
        private M parent;

        public ItemRemovedEventImpl(M parent, M source) {
            super(source);
            this.parent = parent;
        }

        @Override
        public M getRemovedFrom() {
            return parent;
        }

        @Override
        public M getRemovedItem() {
            return getSourceItem();
        }
    }

    public static class ItemAttachedEventImpl<M extends HierarchyItem<M, N>, N>
            extends AbstractItemEvent<M, N> implements
            HierarchyItem.ItemAttachedEvent<M, N> {
        private M parent;

        public ItemAttachedEventImpl(M parent, M source) {
            super(source);
            this.parent = parent;
        }

        @Override
        public M getAddTo() {
            return parent;
        }

        @Override
        public M getNewItem() {
            return getSourceItem();
        }
    }
}
