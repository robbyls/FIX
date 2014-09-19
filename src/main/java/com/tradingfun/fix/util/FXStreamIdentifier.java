package com.tradingfun.fix.util;

public class FXStreamIdentifier {

	private String baseCurrency;
	private String quoteCurrency;
	private String term;

	public FXStreamIdentifier(String baseCurrency, String quoteCurrency, String term) {
		super();
		this.baseCurrency = baseCurrency;
		this.quoteCurrency = quoteCurrency;
		this.term = term;
	}

	public String getBaseCurrency() {
		return baseCurrency;
	}

	public void setBaseCurrency(String baseCurrency) {
		this.baseCurrency = baseCurrency;
	}

	public String getQuoteCurrency() {
		return quoteCurrency;
	}

	public void setQuoteCurrency(String quoteCurrency) {
		this.quoteCurrency = quoteCurrency;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((baseCurrency == null) ? 0 : baseCurrency.hashCode());
		result = prime * result + ((quoteCurrency == null) ? 0 : quoteCurrency.hashCode());
		result = prime * result + ((term == null) ? 0 : term.hashCode());
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
		FXStreamIdentifier other = (FXStreamIdentifier) obj;
		if (baseCurrency == null) {
			if (other.baseCurrency != null)
				return false;
		} else if (!baseCurrency.equals(other.baseCurrency))
			return false;
		if (quoteCurrency == null) {
			if (other.quoteCurrency != null)
				return false;
		} else if (!quoteCurrency.equals(other.quoteCurrency))
			return false;
		if (term == null) {
			if (other.term != null)
				return false;
		} else if (!term.equals(other.term))
			return false;
		return true;
	}

	
	
}
