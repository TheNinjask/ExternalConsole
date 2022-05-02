package pt.theninjask.externalconsole.event;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BasicEvent implements Event {

	private UUID id;

	private String name;

	private AtomicBoolean cancelled;

	private final boolean isCancellable;
	
	//private boolean isFinished;

	//private List<Runnable> afterEvent;

	public BasicEvent() {
		this(BasicEvent.class.getSimpleName(), true);
	}
	
	public BasicEvent(boolean isCancellable) {
		this(BasicEvent.class.getSimpleName(), isCancellable);
	}
	
	public BasicEvent(String name) {
		this(name, true);
	}
	
	public BasicEvent(String name, boolean isCancellable) {
		this.id = UUID.randomUUID();
		this.name = name;
		this.cancelled = new AtomicBoolean();
		this.isCancellable = isCancellable;
		//this.afterEvent = new ArrayList<>();
		//this.isFinished = false;
	}

	public String getName() {
		return name;
	}

	public String getID() {
		return id.toString();
	}

	public void setCancelled(boolean cancelled) {
		if(cancelled && isCancellable)
			this.cancelled.set(cancelled);
	}

	public boolean isCancelled() {
		return cancelled.get();
	}
	
	public boolean isCancellable() {
		return isCancellable;
	}

	/*public synchronized void addAfterEvent(Runnable run) {
		if (isFinished)
			run.run();
		else
			afterEvent.add(run);
	}*/

	/*public synchronized void finishedEvent() {
		isFinished = true;
		afterEvent.forEach(run -> {
			run.run();
		});
	}*/

}
