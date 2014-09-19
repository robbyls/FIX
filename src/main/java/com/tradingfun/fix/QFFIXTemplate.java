package com.tradingfun.fix;

import java.util.List;
import java.util.Map;

public class QFFIXTemplate {

	private Map<String, String> parameterMap;
	private List<QFFIXTemplateTag> tags;
	
	public Map<String, String> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, String> parameterMap) {
		this.parameterMap = parameterMap;
	}

	public List<QFFIXTemplateTag> getTags() {
		return tags;
	}

	public void setTags(List<QFFIXTemplateTag> tags) {
		this.tags = tags;
	}
	
}
