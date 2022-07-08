package pt.theninjask.externalconsole.console.command.util;

public enum Bytes {
	TERABYTE("TB", null), GIGABYTE("GB", TERABYTE), MEGABYTE("MB", GIGABYTE), KILOBYTE("KB", MEGABYTE),
	BYTE("B", KILOBYTE);

	private String sigla;
	private Bytes bigger;

	private Bytes(String sigla, Bytes bigger) {
		this.sigla = sigla;
		this.bigger = bigger;
	}

	public String toString() {
		return sigla;
	}

	public Bytes getBigger() {
		return bigger;
	}
	
	public static Object[] roundByteSize(double size) {
		return roundByteSize(size, Bytes.BYTE);
	}
	
	public static Object[] roundByteSize(double size, Bytes sizeType) {
		while (size > 1023 && sizeType.getBigger() != null) {
			size = size / 1024;
			sizeType = sizeType.getBigger();
		}
		return new Object[] { size, sizeType };
	}
}
