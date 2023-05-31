package cn.zwq.exection;

public class TransactionCommitException extends RuntimeException {
	public TransactionCommitException(String msg) {
		super(msg);
	}
}
