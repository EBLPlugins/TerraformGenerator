package org.terraform.schematic;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.RedstoneWire.Connection;
import org.bukkit.util.Vector;
import org.terraform.coregen.BlockDataFixerAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.version.Version;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

public class TerraSchematic {
	public static HashMap<String, HashMap<Vector,BlockData>> cache = new HashMap<>();
    public SchematicParser parser = new SchematicParser();
    HashMap<Vector, BlockData> data = new HashMap<>();
    SimpleBlock refPoint;
    BlockFace face = BlockFace.NORTH;
    private double VERSION_VALUE;

    //North is always the default blockface.
    //
    public TerraSchematic(SimpleBlock vector) {
        this.refPoint = vector;
    }

    public TerraSchematic(Location loc) {
        this.refPoint = new SimpleBlock(loc);
    }
    
    public TerraSchematic clone(SimpleBlock refPoint) {
    	TerraSchematic clone = new TerraSchematic(refPoint);
    	clone.data = new HashMap<>();
    	for(Entry<Vector, BlockData> entry:data.entrySet()) {
    		clone.data.put(entry.getKey(),entry.getValue());
    	}
    	clone.VERSION_VALUE = VERSION_VALUE;
    	return clone;
    }

    public static TerraSchematic load(String internalPath, SimpleBlock refPoint) throws FileNotFoundException {

    	//A new object gets created from here. If the path is in the cache,
    	//this object is NOT the one that gets returned. Instead, a clone is
    	//returned, with a new hashmap copy and a (broken) version number.
    	TerraSchematic schem = new TerraSchematic(refPoint);
        if(cache.containsKey(internalPath)) { 
        	schem.data = cache.get(internalPath);
        	return schem.clone(refPoint);
        }
        
        InputStream is = TerraformGeneratorPlugin.get().getClass().getResourceAsStream("/" + internalPath + ".terra");
        @SuppressWarnings("resource")
        Scanner sc = new Scanner(is);    //file to be scanned

        String line = sc.nextLine(); //First line is the schematic's version.
        schem.VERSION_VALUE = Version.toVersionDouble(line);
        
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            if (line.isEmpty()) continue;
            String[] cont = line.split(":@:");
            String[] v = cont[0].split(",");
            Vector key = new Vector(Integer.parseInt(v[0]), Integer.parseInt(v[1]), Integer.parseInt(v[2]));
            BlockData value;
            try {
                value = Bukkit.createBlockData(cont[1]);
                //TerraformGeneratorPlugin.logger.info("loaded: " + value.getAsString());
            } catch (IllegalArgumentException e) {
                BlockDataFixerAbstract fixer = TerraformGeneratorPlugin.injector.getBlockDataFixer();
                if (fixer != null) {
                    value = Bukkit.createBlockData(fixer.updateSchematic(schem.getVersionValue(),cont[1]));
                    
                } else {
                    //GG
                    value = null;
                    throw e;
                }
            }
            schem.data.put(key, value);
        }
        sc.close();
        
        //Cache all small schematics
        if(schem.data.size() < 100)
        	cache.put(internalPath, schem.data);
        return schem;
    }

    public void registerBlock(Block b) {
        Vector rel = b.getLocation().toVector().subtract(refPoint.toVector());
        //String coords = rel.getBlockX() + "," + rel.getBlockY() + "," + rel.getBlockZ();
        data.put(rel, b.getBlockData());
    }

    public void apply() {
        BlockDataFixerAbstract bdfa = TerraformGeneratorPlugin.injector.getBlockDataFixer();
        ArrayList<Vector> multiFace = new ArrayList<>();
        
        for (Entry<Vector, BlockData> entry : data.entrySet()) {
            Vector pos = entry.getKey().clone();
            BlockData bd = entry.getValue().clone();
            if (face == BlockFace.WEST) {
                int x = pos.getBlockX();
                pos.setX(pos.getZ());
                pos.setZ(x * -1);
            } else if (face == BlockFace.SOUTH) {
                pos.setX(pos.getX() * -1);
                pos.setZ(pos.getZ() * -1);
            } else if (face == BlockFace.EAST) {
                int x = pos.getBlockX();
                pos.setX(pos.getZ() * -1);
                pos.setZ(x);
            }

            if (face != BlockFace.NORTH) {
                if (bd instanceof Orientable) {
                    Orientable o = (Orientable) bd;
                    if (face == BlockFace.EAST || face == BlockFace.WEST) {
                        if (o.getAxis() == Axis.X) {
                            o.setAxis(Axis.Z);
                        } else if (o.getAxis() == Axis.Z) {
                            o.setAxis(Axis.X);
                        }
                    }
                } else if (bd instanceof Rotatable) {
                    Rotatable r = (Rotatable) bd;
                    if (face == BlockFace.SOUTH) {
                        r.setRotation(r.getRotation().getOppositeFace());
                    } else if (face == BlockFace.EAST) {
                        r.setRotation(BlockUtils.getAdjacentFaces(r.getRotation())[0]);
                    } else if (face == BlockFace.WEST) {
                        r.setRotation(BlockUtils.getAdjacentFaces(r.getRotation())[1]);
                    }
                } 
                else if (bd instanceof Directional) {
                    Directional r = (Directional) bd;
                    if (BlockUtils.isDirectBlockFace(r.getFacing()))
                        if (face == BlockFace.SOUTH) {
                        	//South means flip it to opposite face
                        	//TerraformGeneratorPlugin.logger.info(r.getMaterial() + ":" + r.getFacing() + " ->" + r.getFacing().getOppositeFace());
                            r.setFacing(r.getFacing().getOppositeFace());
                        } else if (face == BlockFace.WEST) {
                        	//Turn left
                        	//TerraformGeneratorPlugin.logger.info(r.getMaterial() + ":" + r.getFacing() + " ->" + BlockUtils.getAdjacentFaces(r.getFacing())[1]);
                            r.setFacing(BlockUtils.getAdjacentFaces(r.getFacing())[1]);
                        } else if (face == BlockFace.EAST) {
                        	//Turn right
                        	//TerraformGeneratorPlugin.logger.info(r.getMaterial() + ":" + r.getFacing() + " ->" + BlockUtils.getAdjacentFaces(r.getFacing())[0]);
                            r.setFacing(BlockUtils.getAdjacentFaces(r.getFacing())[0]);
                        }
                } 
                else if (bd instanceof MultipleFacing) {
                    multiFace.add(pos);
                }
                else if (bd instanceof RedstoneWire) {
                	RedstoneWire w = (RedstoneWire) bd;
                	RedstoneWire newData = (RedstoneWire) Bukkit.createBlockData(Material.REDSTONE_WIRE);
                	newData.setPower(w.getPower());
                	for(BlockFace wireFace:w.getAllowedFaces())
                	{
                		if(w.getFace(wireFace) == Connection.NONE) continue;
                		
                		if (this.face == BlockFace.SOUTH) {
                        	//South means flip it to opposite face
                			newData.setFace(wireFace.getOppositeFace(), w.getFace(wireFace));
                        } else if (this.face == BlockFace.WEST) {
                        	//Turn left
                        	newData.setFace(BlockUtils.getAdjacentFaces(wireFace)[1], w.getFace(wireFace));
                        } else if (this.face == BlockFace.EAST) {
                        	//Turn right
                        	newData.setFace(BlockUtils.getAdjacentFaces(wireFace)[0], w.getFace(wireFace));
                        }
                        else
                        	newData.setFace(wireFace, w.getFace(wireFace));
                	}
                	bd = newData;
                }

                //Apply version-specific updates
                if (bdfa != null)
                    bdfa.correctFacing(pos, null, bd, face);
            }
            
//            if(pos.getBlockX() == 0 && pos.getBlockZ() == 0) {
//            	Bukkit.getLogger().info("DEBUG SCHEMATIC: " + refPoint.getRelative(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()).toVector().toString());
//            }
            parser.applyData(refPoint.getRelative(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()), bd);
        }
        
        parser.applyDelayedData();
        //Multiple-facing blocks are just gonna be painful.
        for (Vector pos : multiFace) {
            SimpleBlock b = refPoint.getRelative(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
            if(b.getBlockData() instanceof MultipleFacing)
            	BlockUtils.correctSurroundingMultifacingData(b);
        }
        if (bdfa != null && face != BlockFace.NORTH) {
            for (Vector pos : bdfa.flush()) {
                SimpleBlock b = refPoint.getRelative(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
                bdfa.correctFacing(pos, b, null, face);
            }
        }
    }

    public void export(String path) throws IOException {
        File fout = new File(path);
        //fout.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(fout);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        
        //Append version header
        bw.write(Version.DOUBLE+"");
        bw.newLine();
        
        for (Entry<Vector, BlockData> entry : data.entrySet()) {
            String v = entry.getKey().getBlockX() + "," + entry.getKey().getBlockY() + ',' + entry.getKey().getBlockZ();
            bw.write(v + ":@:" + entry.getValue().getAsString());
            bw.newLine();
        }

        bw.close();
    }

    /**
     * @return the face
     */
    public BlockFace getFace() {
        return face;
    }

    /**
     * @param face the face to set
     */
    public void setFace(BlockFace face) {
        this.face = face;
    }

    /**
     * This doesn't appear to be used at all
     * lmao.
     * @return
     */
	public double getVersionValue() {
		return VERSION_VALUE;
	}

}
