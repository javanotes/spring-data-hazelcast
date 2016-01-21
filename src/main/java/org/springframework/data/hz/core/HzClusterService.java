package org.springframework.data.hz.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hz.PartitionMigrationCallback;
import org.springframework.util.StringUtils;

import com.hazelcast.config.ConfigurationException;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.core.MigrationEvent;
import com.hazelcast.core.MigrationListener;
import com.hazelcast.map.listener.MapListener;

/**
 * This is the class responsible for maintaining the peer to peer clustering using Hazelcast. 
 * It is different from other listeners since this will be active throughout the VM lifetime
 * @author esutdal
 *
 */
public final class HzClusterService {
	
	public static final String MSG_PAUSE_INGEST = "MSG_PAUSE_INGEST";
	public static final String MSG_PAUSE_INGEST_ACK = "MSG_PAUSE_INGEST_ACK";
	public static final String MSG_RESUME_INGEST = "MSG_RESUME_INGEST";
	
	private HzInstanceProxy hzInstance = null;
		
	private static final Logger log = LoggerFactory.getLogger(HzClusterService.class);
	
	//we will be needing these for short time tasks.  Since a member addition / removal operation should not occur very frequently
	private final ExecutorService worker = Executors.newCachedThreadPool(new ThreadFactory() {
		private int n = 0;
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "hz.member.thread" + "-" +(n++));
			t.setDaemon(true);
			return t;
		}
	});
	
	public void remove(Object key, String map)
  {
	  hzInstance.remove(key, map);
  }
  
  public <K, V> Map<K, V> getMap(String map)
  {
    return hzInstance.getMap(map);
  }
  public Object put(Object key, Object value, String map) {
    return hzInstance.put(key, value, map);
    
  }
  public void set(Object key, Object value, String map) {
    hzInstance.set(key, value, map);
    
  }
  public Object get(Object key, String map) {
    return hzInstance.get(key, map);
    
  }
  Set<Entry<Object, Object>> getAll(String map)
  {
    return hzInstance.getAll(map);
  }
			
				
	/**
	 * To get notifications for member events
	 * @author esutdal
	 *
	 */
	private class InstanceListener implements MembershipListener{

		@Override
		public void memberRemoved(final MembershipEvent event) {
			
								
			worker.execute(new Runnable() {
				
				@Override
				public void run() {
										
					if(plannedShutDownRecvd.isEmpty()){
						log.debug("Detected forced shutdown of member: " + event.getMember().toString());
						
						//This is a force kill of instance.
						//so do need to bring up another instance
						//this code can be used only if we are using a single node with multiple jvms
						//ideally the instances will reside on different nodes. there we would need
						//manual intervention or monitor scripts to bring up a new instance
												
						//cluster.tryRestartMember(event.getMember());
						
					}
					else{
						log.debug("Detected planned shutdown of member: " + event.getMember().toString());
						synchronized(plannedShutDownRecvd){
							plannedShutDownRecvd.remove();
						}
						//this else block should be reached by only one component, the one which successfully deques
						//this is a planned shutdown. do not bring up instance
						//but we don't know whether the primary was the victim!
						//so try to run fail-over
						
						//do nothing
					}
					
				}
			});
			
		}
		
		@Override
		public void memberAdded(final MembershipEvent event) {
			
								
			worker.execute(new Runnable() {
				
				@Override
				public void run() {
					log.info("Detected startup of member: " + event.getMember());
					
				}
			});
		}

		@Override
		public void memberAttributeChanged(MemberAttributeEvent arg0) {
			
		}
		
	}
		
	private final AtomicBoolean migrationRunning = new AtomicBoolean();	
	/**
	 * Is migration ongoing
	 * @return
	 */
	public boolean isMigrationRunning() {
		return migrationRunning.compareAndSet(true, true);
	}

	private final Map<String, PartitionMigrationCallback<?>> migrCallbacks = new HashMap<>();
	/**
	 * 
	 * @param callback
	 * @throws IllegalAccessException 
	 */
	public void addPartitionMigrationCallback(PartitionMigrationCallback<?> callback) throws IllegalAccessException
	{
	  if (!started) {
      migrCallbacks.put(callback.keyspace(), callback);
    }
	  else
      throw new IllegalAccessException("PartitionMigrationListener cannot be added after Hazelcast service has been started");
	}
	private final LinkedList<Integer> plannedShutDownRecvd = new LinkedList<Integer>();
	/**
	 * 
	 * @param keyspace
	 * @param listener
	 * @throws IllegalAccessException 
	 */
	public void addLocalEntryListener(Serializable keyspace, MapListener listener)
  {
	  hzInstance.addLocalEntryListener(keyspace.toString(), listener);
  }
	private void init()
	{
	  hzInstance.init(new InstanceListener());
	  
    hzInstance.addMigrationListener(new MigrationListener() {
      
      @Override
      public void migrationStarted(MigrationEvent migrationevent) {
        migrationRunning.getAndSet(true);
      }
      
      @Override
      public void migrationFailed(MigrationEvent migrationevent) {
        migrationRunning.getAndSet(false);
        synchronized (migrationRunning) {
          migrationRunning.notifyAll();
        }
      }
      
      @Override
      public void migrationCompleted(MigrationEvent migrationevent) 
      {
        migrationRunning.getAndSet(false);
        synchronized (migrationRunning) {
          migrationRunning.notifyAll();
        }
        if(migrationevent.getNewOwner().localMember())
        {
          log.info(">>>>>>>>>>>>>>>>> Migration detected of partition ..."+migrationevent.getPartitionId());
          for(Entry<String, PartitionMigrationCallback<?>> e : migrCallbacks.entrySet())
          {
            IMap<Serializable, Object> map = hzInstance.getMap(e.getKey());
            Set<Serializable> keys = new HashSet<>();
            for(Serializable key : map.localKeySet())
            {
              if(hzInstance.getPartitionIDForKey(key) == migrationevent.getPartitionId())
              {
                keys.add(key);
              }
            }
            if(!keys.isEmpty())
            {
              
              map.executeOnKeys(keys, e.getValue());
            }
          }
          
        }
        
      }
    });
	}
	/**
	 * Public constructor
	 * @param props
	 * @throws ConfigurationException
	 */
	private HzClusterService(String cfgXml) {
		if(hzInstance == null)
		{
			hzInstance = StringUtils.hasText(cfgXml) ? new HzInstanceProxy(cfgXml) : new HzInstanceProxy();
		}
	}
		
	private static HzClusterService singleton;
	
	/**
	 * 
	 * @param cfXml
	 * @return
	 */
	public static HzClusterService instance(String cfXml)
  {
    if(singleton == null)
    {
      synchronized (HzClusterService.class) {
        if(singleton == null)
        {
          singleton = new HzClusterService(cfXml);
        }
      }
    }
    return singleton;
  }

	private volatile boolean started;
	/**
	 * 
	 * @return
	 */
	public boolean isStarted() {
    return started;
  }

  /**
	 * 
	 */
	public void startService()
	{
	  if (!started) {
      init();
      started = true;//silently ignore
    }
	}
	
	/**
	 * No of members at this point
	 * @return
	 */
	public int size()
	{
		return hzInstance.noOfMembers();
	}
	/**
	 * 
	 * @param retry
	 */
	public void stopService(boolean retry) {
		if(hzInstance != null)
		{
			hzInstance.stop("");
		}
		worker.shutdown();
		try {
			worker.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			
		}
		
		log.info("Stopped cluster listener");
	}

  public Object removeNow(Serializable id, String string) {
    return hzInstance.removeNow(id, string);
    
  }
	
	
	
}
