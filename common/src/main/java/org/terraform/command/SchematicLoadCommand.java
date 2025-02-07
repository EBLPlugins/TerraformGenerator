package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.SchematicArgument;
import org.terraform.command.contants.TerraCommand;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.utils.BlockUtils;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class SchematicLoadCommand extends TerraCommand {

    public SchematicLoadCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
        this.parameters.add(new SchematicArgument("schem-name", false));
    }

    @Override
    public String getDefaultDescription() {
        return "Loads and pastes schematic from inside the jar file";
    }

    @Override
    public boolean canConsoleExec() {
        return false;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {

        return sender.isOp();
    }

    @Override
    public void execute(CommandSender sender, Stack<String> args)
            throws InvalidArgumentException {
        ArrayList<Object> parsed = this.parseArguments(sender, args);
        TerraSchematic schem = (TerraSchematic) parsed.get(0);
        sender.sendMessage("Schematic Version: " + schem.getVersionValue());
        schem.setFace(BlockUtils.getDirectBlockFace(new Random()));
        sender.sendMessage("Facing: " + schem.getFace());
        schem.apply();
    }
}
