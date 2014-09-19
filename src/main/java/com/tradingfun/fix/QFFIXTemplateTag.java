package com.tradingfun.fix;

import java.util.List;

public class QFFIXTemplateTag {
	
	private String key;
	private String value;
	private boolean isHeader;

	private List<QFFIXTemplateTag> repeatGroup;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<QFFIXTemplateTag> getRepeatGroup() {
		return repeatGroup;
	}

	public void setRepeatGroup(List<QFFIXTemplateTag> repeatGroup) {
		this.repeatGroup = repeatGroup;
	}

	public boolean isHeader() {
		return isHeader;
	}

	public void setHeader(boolean isHeader) {
		this.isHeader = isHeader;
	}
	
}
