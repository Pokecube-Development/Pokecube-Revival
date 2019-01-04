package pokecube.compat.opencomputers;

import li.cil.oc.api.Driver;
import net.minecraftforge.fml.common.Optional.Method;
import pokecube.compat.opencomputers.drivers.CommanderDriver;
import pokecube.compat.opencomputers.drivers.PCDriver;
import pokecube.compat.opencomputers.drivers.TMMachineDriver;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;

public class OCCompat
{
    @Method(modid = "opencomputers")
    @CompatClass(phase = Phase.INIT)
    public static void InitOC()
    {
        Driver.add(new PCDriver());
        Driver.add(new TMMachineDriver());
        Driver.add(new CommanderDriver());
    }
}
