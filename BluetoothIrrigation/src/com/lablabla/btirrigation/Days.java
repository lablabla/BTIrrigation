package com.lablabla.btirrigation;

import java.io.Serializable;

public class Days implements Serializable {

	/**
	 * 
	 */
	public static final long serialVersionUID = 1L;
	public static final int BIT_SUNDAY = 1;
	public static final int BIT_MONDAY = 2;
	public static final int BIT_TUESDAY = 4;
	public static final int BIT_WEDENSDAY = 8;
	public static final int BIT_THURSDAY = 16;
	public static final int BIT_FRIDAY = 32;
	public static final int BIT_SATURDAY = 64;

	private int start1H, start1M, stop1H, stop1M, start2H, start2M, stop2H,
			stop2M;
	private byte days;

	public Days() {
		days = 0;
	}

	public void setDays(boolean sunday, boolean monday, boolean tuesday,
			boolean wedensday, boolean thursday, boolean friday,
			boolean saturday) {
		if (sunday) {
			days |= BIT_SUNDAY;
		}
		if (monday) {
			days |= BIT_MONDAY;
		}
		if (tuesday) {
			days |= BIT_TUESDAY;
		}
		if (wedensday) {
			days |= BIT_WEDENSDAY;
		}
		if (thursday) {
			days |= BIT_THURSDAY;
		}
		if (friday) {
			days |= BIT_FRIDAY;
		}
		if (saturday) {
			days |= BIT_SATURDAY;
		}
	}

	public int getStart1H() {
		return start1H;
	}

	public void setStart1H(int start1H) {
		this.start1H = start1H;
	}

	public int getStart1M() {
		return start1M;
	}

	public void setStart1M(int start1M) {
		this.start1M = start1M;
	}

	public int getStop1H() {
		return stop1H;
	}

	public void setStop1H(int stop1H) {
		this.stop1H = stop1H;
	}

	public int getStop1M() {
		return stop1M;
	}

	public void setStop1M(int stop1M) {
		this.stop1M = stop1M;
	}

	public int getStart2H() {
		return start2H;
	}

	public void setStart2H(int start2H) {
		this.start2H = start2H;
	}

	public int getStart2M() {
		return start2M;
	}

	public void setStart2M(int start2M) {
		this.start2M = start2M;
	}

	public int getStop2H() {
		return stop2H;
	}

	public void setStop2H(int stop2H) {
		this.stop2H = stop2H;
	}

	public int getStop2M() {
		return stop2M;
	}

	public void setStop2M(int stop2M) {
		this.stop2M = stop2M;
	}

	public byte getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = (byte) days;
	}
}
