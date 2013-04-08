package org.squirrelframework.foundation.data;

import java.util.List;

import org.squirrelframework.foundation.component.Observable;
import org.squirrelframework.foundation.event.SquirrelEvent;

/**
 * Generic tree structure interface which handles basic tree structure
 * operations, e.g. get children items, get parent item, set item value and so
 * on. Support event notifications for tree item operations, e.g. attach new
 * item, detach old item and set new item value.
 * 
 * @author Henry.He
 * 
 * @param <M>
 *            the type of tree item
 * @param <N>
 *            the type of item value
 */
public interface HierarchyItem<M extends HierarchyItem<M, N>, N> extends Observable {

    List<M> getChildren();

    List<M> getRawChildren();

    List<M> getAllRawChildren();

    boolean hasChildren();

    M getParent();

    boolean isRoot();

    M getRoot();

    M detach();

    N getValue();

    Class<?> getType();

    void setValue(N newValue);

    List<M> getAllChildren();

    List<M> getAllChildren(boolean includeThis, boolean isDepthFirst);

    List<M> getAllRawChildren(boolean includeThis, boolean isDepthFirst);

    <T extends ItemProvider<M, N>> T getItemProvider(Class<T> provideType);

    boolean isLeaf();

    void setData(String key, Object newData);

    Object removeData(String key);

    Object getData(String key);

    void clearData();

    String toLog();

    /**
     * <p>
     * The {@code ItemEventListener} listens to all the events that are extended
     * from {@link ItemEvent}.
     * </p>
     * 
     * <pre>
     * item.addListener(ItemEvent.class,
     *         new TreeItem.ItemEventListener&lt;TreeNode, BaseEntity&gt;() {
     *             &#064;Override
     *             public void itemEvent(ItemEvent&lt;TreeNode, BaseEntity&gt; event) {
     *                 // process event logic ...
     *             }
     *         });
     * </pre>
     * 
     */
    interface ItemEventListener<M extends HierarchyItem<M, N>, N> {
        void itemEvent(ItemEvent<M, N> event);
    }

    /**
     * Base event of all the tree item events.
     */
    interface ItemEvent<M extends HierarchyItem<M, N>, N> extends SquirrelEvent {
        /**
         * @return tree item that event happens on
         */
        M getSourceItem();
    }

    void addListener(ItemEventListener<M, N> listener);

    void removeListener(ItemEventListener<M, N> listener);

    /**
     * The {@code ValueChangeListener} listens to {@link ValueChangeEvent}.
     */
    interface ValueChangeListener<M extends HierarchyItem<M, N>, N> {
        void valueChange(ValueChangeEvent<M, N> event);
    }

    /**
     * The {@code ValueChangeEvent} was fired when new value was set to tree
     * item.
     */
    interface ValueChangeEvent<M extends HierarchyItem<M, N>, N> extends
            ItemEvent<M, N> {
        /**
         * @return old value of tree item.
         */
        N getOldValue();

        /**
         * @return new value of tree item.
         */
        N getNewValue();
    }

    void addListener(ValueChangeListener<M, N> listener);

    void removeListener(ValueChangeListener<M, N> listener);

    /**
     * The {@code ItemRemovedListener} listens to {@link ItemRemovedEvent}.
     */
    interface ItemRemovedListener<M extends HierarchyItem<M, N>, N> {
        void itemRemoved(ItemRemovedEvent<M, N> event);
    }

    /**
     * The {@code ItemRemovedEvent} was fired when existed tree item was removed
     * from the tree.
     */
    interface ItemRemovedEvent<M extends HierarchyItem<M, N>, N> extends
            ItemEvent<M, N> {
        /**
         * @return parent of removed item
         */
        M getRemovedFrom();

        /**
         * @return removed item
         */
        M getRemovedItem();
    }

    void addListener(ItemRemovedListener<M, N> listener);

    void removeListener(ItemRemovedListener<M, N> listener);

    /**
     * The {@code ItemAttachedListener} listens to {@link ItemAttachedEvent}.
     */
    interface ItemAttachedListener<M extends HierarchyItem<M, N>, N> {
        void itemAttached(ItemAttachedEvent<M, N> event);
    }

    /**
     * The {@code ItemAttachedEvent} was fired when new tree item was attached
     * to the tree.
     */
    interface ItemAttachedEvent<M extends HierarchyItem<M, N>, N> extends
            ItemEvent<M, N> {
        /**
         * @return parent of new item
         */
        M getAddTo();

        /**
         * @return new item
         */
        M getNewItem();
    }

    void addListener(ItemAttachedListener<M, N> listener);

    void removeListener(ItemAttachedListener<M, N> listener);
}
