package tennox.customselectionbox;

public class CSBFloat {

	private float value;
	private float min, max;
	private float mcDefault, csbDefault;

	private String name;
	private float maxInt;

	public CSBFloat(String name, float maxInt, float mcDefault, float csbDefault) {
		this.name = name;
		this.mcDefault = mcDefault;
		this.csbDefault = csbDefault;
		this.maxInt = maxInt;
		this.min = 0f;
		this.max = 1f;
		validate();
	}

	public String displayString() {
		return name + ": " + getAsInt();
	}

	public float sliderVal() {
		validate();
		return (get() - min) / (max - min);
	}

	public void setFromSlider(float sliderValue) {
		set(sliderValue * (max - min) + min);
	}

	private void validate() {
		if (value < min)
			value = min;
		if (value > max)
			value = max;
	}

	public CSBFloat setMinMax(float min, float max) {
		this.min = min;
		this.max = max;
		validate();
		return this;
	}

	public int getAsInt() {
		return Math.round(get() * (maxInt / max));
	}

	public void setFromInt(int val) {
		set((float) val / ((float) maxInt / max));
	}

	public float get() {
		validate();
		return value;
	}

	public void set(float val) {
		value = val;
		validate();
	}

	public void mcDefault() {
		this.set(mcDefault);
	}

	public void csbDefault() {
		this.set(csbDefault);
	}

	public String toString() {
		return name + "[" + get() + ", min=" + min + ", max=" + max + "|" + maxInt + " mc=" + mcDefault + " csb=" + csbDefault + "]";
	}
}
