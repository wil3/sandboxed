package edu.bu.sandboxed.net;

public class FileTransferException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1356446839767863892L;

	public FileTransferException(){
		super();
	}
	public FileTransferException(String message){
		super(message);
	}
	public FileTransferException(Throwable throwable){
		super(throwable);
	}
}
