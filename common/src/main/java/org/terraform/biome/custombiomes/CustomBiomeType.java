package org.terraform.biome.custombiomes;

import org.terraform.utils.version.Version;

public enum CustomBiomeType {
	NONE,
	MUDDY_BOG("b8ad49","9c8046","b8ad49","d9cd62","ad8445","ad8445", 0.8f, false),
	CHERRY_GROVE("","69faff","","87fffb","ffa1fc","acff96", 0.8f, false),
	SCARLET_FOREST("","","","","fc3103","ff7700", 0.8f, false),
	CRYSTALLINE_CLUSTER("e54fff","c599ff","e54fff","","","", 0.8f, false),
	;
	
	private String key;
	private String fogColor;
	private String waterColor;
	private String waterFogColor;
	private String skyColor;
	private String foliageColor;
	private String grassColor;
	private float rainFall = 0.8f;
	private boolean isCold = false;

	private CustomBiomeType() {
		this.key = "terraformgenerator:" + this.toString().toLowerCase();
		this.fogColor = "";
		this.waterColor = "";
		this.waterFogColor = "";
		this.skyColor = "";
		this.foliageColor = "";
		this.grassColor = "";
	}
	
	CustomBiomeType(String fogColor, String waterColor, String waterFogColor,
			String skyColor, String foliageColor, String grassColor, float rainFall,
			boolean isCold) {
		this.key = "terraformgenerator:" + this.toString().toLowerCase();
		this.fogColor = fogColor;
		this.waterColor = waterColor;
		this.waterFogColor = waterFogColor;
		this.skyColor = skyColor;
		this.foliageColor = foliageColor;
		this.grassColor = grassColor;
		this.rainFall = rainFall;
		this.isCold = isCold;
        //In 1.20, cherry trees no longer need the pink.
        if(Version.isAtLeast(20) && this.foliageColor.equals("ffa1fc"))
            this.foliageColor = "acff96";

	}

	public String getKey() {
		return key;
	}

	public String getFogColor() {
		return fogColor;
	}

	public String getWaterColor() {
		return waterColor;
	}

	public String getWaterFogColor() {
		return waterFogColor;
	}

	public String getSkyColor() {
		return skyColor;
	}

	public String getFoliageColor() {
		return foliageColor;
	}

	public String getGrassColor() {
		return grassColor;
	}

	public float getRainFall() {
		return rainFall;
	}

	public boolean isCold() {
		return isCold;
	}
}
