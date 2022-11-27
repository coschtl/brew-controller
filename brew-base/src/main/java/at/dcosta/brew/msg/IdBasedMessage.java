package at.dcosta.brew.msg;

import java.io.Serializable;

public interface IdBasedMessage extends Serializable {
	public String getId();

	public String getMessage();
}
