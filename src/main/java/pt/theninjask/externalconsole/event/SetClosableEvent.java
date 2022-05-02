package pt.theninjask.externalconsole.event;

public class SetClosableEvent extends BasicEvent {

	private boolean value;
	
	public SetClosableEvent(boolean toClosable) {
		super(SetClosableEvent.class.getSimpleName(), true);
		this.value = toClosable;
	}
	
	public boolean toClosable() {
		return value;
	}
	
}
