package org.terraform.structure.pillager.mansion;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import java.util.Random;

public class MansionStandardSecondFloorPiece extends JigsawStructurePiece {

    public MansionStandardSecondFloorPiece(int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(widthX, height, widthZ, type, validDirs);
    }

    @Override
    public void build(PopulatorDataAbstract data, Random rand) {
        int[] lowerCorner = this.getRoom().getLowerCorner(0);
        int[] upperCorner = this.getRoom().getUpperCorner(0);

        //Place attic ceiling
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++)
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                data.setType(x, this.getRoom().getY()+this.getRoom().getHeight(), z, Material.DARK_OAK_PLANKS);
                
            }
    }

    @Override
    public void postBuildDecoration(Random random, PopulatorDataAbstract data) {

    }
    
    /**
     * Mansions are complex, and they need a third pass to properly ensure that
     * previous important details were planted before placing the final edge pieces.
     * @param random
     * @param data
     */
    public void thirdStageDecoration(Random random, PopulatorDataAbstract data) {
    	
    }
    

}
