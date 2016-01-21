/* ============================================================================
*
* FILE: HazelcastKeyValueAdaptor.java
*
* MODULE DESCRIPTION:
* See class description
*
* Copyright (C) 2015 
*
* All rights reserved
*
* ============================================================================
*/
package org.springframework.data.hz;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;

import org.springframework.data.hz.core.HzClusterService;
import org.springframework.data.keyvalue.core.AbstractKeyValueAdapter;
import org.springframework.data.keyvalue.core.ForwardingCloseableIterator;
import org.springframework.data.util.CloseableIterator;
import org.springframework.util.Assert;
/**
 * 
 */
public class HazelcastKeyValueAdapter extends AbstractKeyValueAdapter {

  private final HzClusterService hz;
 /**
  * 
  * @param joinImmediate
  * @param xmlCfg
  */
  public HazelcastKeyValueAdapter(boolean joinImmediate, String xmlCfg) {
    hz = HzClusterService.instance(xmlCfg);
    if(joinImmediate)
      join();
  }
  /**
   * 
   */
  public HazelcastKeyValueAdapter() {
    this(false, null);
  }
  /**
   * 
   * @param classpathXmlCfg
   */
  public HazelcastKeyValueAdapter(String classpathXmlCfg) {
    this(false, classpathXmlCfg);
  }
  /**
   * Add a local entry listener on the given map for add/update entry. Local entry listeners
   * can be registered any time.
   * @param <V>
   * @param mapL
   * @param keyspace
   */
  public <V> void addLocalKeyspaceListener(LocalPutMapEntryCallback<V> callback)
  {
    hz.addLocalEntryListener(callback.keyspace(), callback);
  }
  /**
   * Add a partition migration listener on the given map. Migration listeners have to be
   * registered before {@link #join()} is invoked.
   * @param <V>
   * @param callback
   * @throws IllegalAccessException if added after service is already started
   */
  public <V> void addPartitionMigrationListener(PartitionMigrationCallback<V> callback) throws IllegalAccessException
  {
    if (!hz.isStarted()) {
      hz.addPartitionMigrationCallback(callback);
    }
    else
      throw new IllegalAccessException("PartitionMigrationListener cannot be added after Hazelcast service has been started");
  }
  
  /**
   * Starts Hazelcast service and joins to cluster. Basically this registers 
   * the lifecycle listener and any partition migration listeners.
   */
  public void join()
  {
    hz.startService();
  }
  @Override
  public Object put(Serializable id, Object item, Serializable keyspace) {
    if(!hz.isStarted())
      throw new IllegalStateException("Hazelcast service not started!");
    Assert.notNull(id, "Cannot add item with null id.");
    Assert.notNull(keyspace, "Cannot add item for null collection.");
    return hz.put(id, item, keyspace.toString());
  }
  public void set(Serializable id, Object item, Serializable keyspace) {
    if(!hz.isStarted())
      throw new IllegalStateException("Hazelcast service not started!");
    Assert.notNull(id, "Cannot add item with null id.");
    Assert.notNull(keyspace, "Cannot add item for null collection.");
    hz.set(id, item, keyspace.toString());
  }

  @Override
  public boolean contains(Serializable id, Serializable keyspace) {
    if(!hz.isStarted())
      throw new IllegalStateException("Hazelcast service not started!");
    Assert.notNull(id, "Cannot check item with null id.");
    Assert.notNull(keyspace, "Cannot check item for null collection.");
    return hz.getMap(keyspace.toString()).containsKey(id);
  }

  @Override
  public Object get(Serializable id, Serializable keyspace) {
    if(!hz.isStarted())
      throw new IllegalStateException("Hazelcast service not started!");
    Assert.notNull(id, "Cannot get item with null id.");
    Assert.notNull(keyspace, "Cannot get item for null collection.");
    return hz.get(id, keyspace.toString());
  }

  @Override
  public Object delete(Serializable id, Serializable keyspace) {
    if(!hz.isStarted())
      throw new IllegalStateException("Hazelcast service not started!");
    Assert.notNull(id, "Cannot delete item with null id.");
    Assert.notNull(keyspace, "Cannot delete item for null collection.");
    return hz.removeNow(id, keyspace.toString());
  }
  public void deleteAsync(Serializable id, Serializable keyspace) {
    if(!hz.isStarted())
      throw new IllegalStateException("Hazelcast service not started!");
    Assert.notNull(id, "Cannot delete item with null id.");
    Assert.notNull(keyspace, "Cannot delete item for null collection.");
    hz.remove(id, keyspace.toString());
  }

  /**
   * @deprecated Expensive statement.
   */
  @Override
  public Iterable<?> getAllOf(Serializable keyspace) {
    if(!hz.isStarted())
      throw new IllegalStateException("Hazelcast service not started!");
    Assert.notNull(keyspace, "Cannot getAllOf for null collection.");
    return hz.getMap(keyspace.toString()).values();
  }

  
  @SuppressWarnings("unchecked")
  @Override
  public CloseableIterator<Entry<Serializable, Object>> entries(
      Serializable keyspace) {
    if(!hz.isStarted())
      throw new IllegalStateException("Hazelcast service not started!");
    Assert.notNull(keyspace, "Cannot iterate entries for null collection.");
    return new ForwardingCloseableIterator<Entry<Serializable, Object>>((Iterator<? extends Entry<Serializable, Object>>) 
        hz.getMap(keyspace.toString()).entrySet().iterator());
  }

  @Override
  public void deleteAllOf(Serializable keyspace) {
    if(!hz.isStarted())
      throw new IllegalStateException("Hazelcast service not started!");
    Assert.notNull(keyspace, "Cannot deleteAllOf for null collection.");
    hz.getMap(keyspace.toString()).clear();
  }

  /**
   * @deprecated Not implemented
   */
  @Override
  public void clear() {
    System.err.println("<< WARNING: HazelcastKeyValueAdapter.clear() IGNORED >>");
  }

  @Override
  public long count(Serializable keyspace) {
    if(!hz.isStarted())
      throw new IllegalStateException("Hazelcast service not started!");
    Assert.notNull(keyspace, "Cannot count for null collection.");
    return hz.getMap(keyspace.toString()).size();
  }

  @Override
  public void destroy() throws Exception {
    if(!hz.isStarted())
      throw new IllegalStateException("Hazelcast service not started!");
    hz.stopService(false);
    
  }

}
