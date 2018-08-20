package toughasnails.temperature;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import toughasnails.api.temperature.IModifierMonitor;
import toughasnails.api.temperature.Temperature;

public class TemperatureDebugger implements IModifierMonitor
{
    public Map<String, Context> modifiers = new LinkedHashMap();
    
    private boolean showGui = false;
    public int debugTimer;
    
    public int temperatureTimer;
    public int changeTicks;
    public int targetTemperature;

    @Override
    public void addEntry(Context context)
    {
        int difference = -(context.startTemperature.getRawValue() - context.endTemperature.getRawValue());

        if (difference != 0)
        {
            modifiers.put(context.modifierId, context);
        }
    }

    @Override
    public void setTargetTemperature(Temperature temperature)
    {
        this.targetTemperature = temperature.getRawValue();
    }

    /**
     * Sorts the modifier maps and sends them to the client
     */
    public void finalize(EntityPlayerMP player)
    {
        this.debugTimer = 0;
        
        if (this.showGui)
        {
            sortModifiers();
        }
        
        clearModifiers();
    }
    
    private void sortModifiers()
    {
        LinkedList<Map.Entry<String, Context>> entries = new LinkedList<Map.Entry<String, Context>>(modifiers.entrySet());
        Collections.sort(entries, (o1, o2) ->
        {
            int difference1 = o1.getValue().endTemperature.getRawValue() - o1.getValue().startTemperature.getRawValue();
            int difference2 = o2.getValue().endTemperature.getRawValue() - o2.getValue().startTemperature.getRawValue();
            return difference1 > difference2 ? -1 : difference1 < difference2 ? 1 : 0;
        });

        // linked to retain order
        LinkedHashMap<String, Context> sortedMap = new LinkedHashMap();
        entries.forEach(entry -> sortedMap.put(entry.getKey(), entry.getValue()));
        modifiers = sortedMap;
    }
    
    public void clearModifiers()
    {
        modifiers.clear();
    }
    
    public void setGuiVisible(boolean state, EntityPlayerMP updatePlayer)
    {
        this.showGui = state;
        this.debugTimer = 0;
        
    }
    
    public void setGuiVisible(boolean state)
    {
        setGuiVisible(state, null);
    }
    
    public boolean isGuiVisible()
    {
        return this.showGui;
    }
}
