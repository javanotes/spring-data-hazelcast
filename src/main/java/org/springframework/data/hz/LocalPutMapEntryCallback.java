/* ============================================================================
*
* FILE: PartitionMigrationCallback.java
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

import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
/**
 * Local map entry listener on entry addition and updation
 * @param <V>
 */
public interface LocalPutMapEntryCallback<V> extends EntryAddedListener<Serializable, V>, EntryUpdatedListener<Serializable, V>{

  /**
   * Gets the Map for which migrated elements will have a callback
   * @return
   */
  String keyspace();
  
}
