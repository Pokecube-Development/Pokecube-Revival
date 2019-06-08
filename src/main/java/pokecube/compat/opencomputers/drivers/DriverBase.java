package pokecube.compat.opencomputers.drivers;

import com.google.common.graph.Network;

import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;

public abstract class DriverBase extends DriverSidedTileEntity
{
    abstract String getComponentName();

    public static class ManagedTileEntityEnvironment<T> extends AbstractManagedEnvironment
    {
        protected final T tileEntity;

        public ManagedTileEntityEnvironment(final T tileEntity, final String name)
        {
            this.tileEntity = tileEntity;
            setNode(Network.newNode(this, Visibility.Network).withComponent(name).create());
        }
    }
}
