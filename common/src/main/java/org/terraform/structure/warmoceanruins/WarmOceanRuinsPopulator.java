package org.terraform.structure.warmoceanruins;

import org.bukkit.Material;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class WarmOceanRuinsPopulator extends SingleMegaChunkStructurePopulator {

    @Override
    public Random getHashedRandom(TerraformWorld tw, int chunkX, int chunkZ) {
        return tw.getHashedRand(352341322, chunkX, chunkZ);
    }
    
    private boolean rollSpawnRatio(TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 65732),
                (int) (TConfigOption.STRUCTURES_WARMOCEANRUINS_SPAWNRATIO
                        .getDouble() * 10000),
                10000);
    }

    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, BiomeBank biome) {

    	if(biome == (BiomeBank.DEEP_WARM_OCEAN)
        		|| biome == (BiomeBank.WARM_OCEAN)
        		|| biome == (BiomeBank.DEEP_LUKEWARM_OCEAN)
                || biome == (BiomeBank.CORAL_REEF_OCEAN)
                || biome == (BiomeBank.COLD_OCEAN)
                || biome == (BiomeBank.DEEP_COLD_OCEAN)
                || biome == (BiomeBank.FROZEN_OCEAN)
                || biome == (BiomeBank.DEEP_FROZEN_OCEAN)) {

            return rollSpawnRatio(tw,chunkX,chunkZ);
        }
        return false;
    }

    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {

        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        int[] coords = mc.getCenterBiomeSectionBlockCoords(); //getCoordsFromMegaChunk(tw, mc);
        int x = coords[0];//data.getChunkX()*16 + random.nextInt(16);
        int z = coords[1];//data.getChunkZ()*16 + random.nextInt(16);
        //Height set to 50 as plains village will settle its own height.
        int y = GenUtils.getHighestGround(data, x, z);

        spawnWarmOceanRuins(tw,this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ()),
                data,x,y,z);
    }

    public void spawnWarmOceanRuins(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z){
        int numRooms = 10;
        int range = 80;

        //Level One
        Random hashedRand = tw.getHashedRand(x, y, z);
        RoomLayoutGenerator gen = new RoomLayoutGenerator(hashedRand, RoomLayout.RANDOM_BRUTEFORCE, numRooms, x, y, z, range);
        //gen.setPathPopulator(new WarmOceansPathPopulator());
        gen.setRoomMaxX(15);
        gen.setRoomMaxZ(15);
        gen.setRoomMinX(10);
        gen.setRoomMinZ(10);
        gen.setRoomMaxHeight(15);
        gen.setCarveRooms(true);
        gen.setCarveRoomsMultiplier(0,0,0); //No carving

        gen.forceAddRoom(25, 25, 25); //At least one room that is the center room.

        gen.registerRoomPopulator(new WarmOceanDomeHutRoom(random, false, false));
        gen.registerRoomPopulator(new WarmOceanAltarRoom(random, false, false));
        gen.registerRoomPopulator(new WarmOceanWellRoom(random, false, false));
        gen.registerRoomPopulator(new WarmOceanLargeArcRoom(random, false, false));
        gen.generate();
        gen.fillRoomsOnly(data, tw, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS);

    }

    @Override
    public int getChunkBufferDistance() {
        return 0;
    }

    //Since the cold ocean ruins weren't added, the warm ocean ruins
    //spawn everywhere.
    @Override
    public boolean isEnabled() {
        return (BiomeBank.isBiomeEnabled(BiomeBank.DEEP_WARM_OCEAN)
        		|| BiomeBank.isBiomeEnabled(BiomeBank.WARM_OCEAN)
        		|| BiomeBank.isBiomeEnabled(BiomeBank.DEEP_LUKEWARM_OCEAN)
        		|| BiomeBank.isBiomeEnabled(BiomeBank.CORAL_REEF_OCEAN)
                || BiomeBank.isBiomeEnabled(BiomeBank.COLD_OCEAN)
                || BiomeBank.isBiomeEnabled(BiomeBank.DEEP_COLD_OCEAN)
                || BiomeBank.isBiomeEnabled(BiomeBank.FROZEN_OCEAN)
                || BiomeBank.isBiomeEnabled(BiomeBank.DEEP_FROZEN_OCEAN))
        		&& TConfigOption.STRUCTURES_WARMOCEANRUINS_ENABLED.getBoolean();
    }
}
