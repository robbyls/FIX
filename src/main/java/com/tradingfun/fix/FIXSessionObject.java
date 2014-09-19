package com.tradingfun.fix;

public class FIXSessionObject {
	
	private String sender;
	private String target;
			
	
	public FIXSessionObject(String sender, String target) {
		super();
		this.sender = sender;
		this.target = target;
	}
	
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sender == null) ? 0 : sender.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FIXSessionObject other = (FIXSessionObject) obj;
		if (sender == null) {
			if (other.sender != null)
				return false;
		} else if (!sender.equals(other.sender))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FIXSessionObject [sender=" + sender + ", target=" + target + "]";
	}

	
	public String getIdentifier()
	{
		return sender + "/" + target;
	}
}
