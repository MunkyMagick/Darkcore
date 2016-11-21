package io.darkcraft.darkcore.mod.network;

import java.util.HashMap;

import io.darkcraft.darkcore.mod.DarkcoreMod;
import io.darkcraft.darkcore.mod.interfaces.IDataPacketHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.ForgeSubscribe;

public class PacketHandler
{
	HashMap<String, IDataPacketHandler>	handlers	= new HashMap<String, IDataPacketHandler>();

	@ForgeSubscribe
	public void handleCustomClientPacket(ClientCustomPacketEvent event)
	{
		handleCustomPacket(event);
	}

	@ForgeSubscribe
	public void handleCustomServerPacket(ServerCustomPacketEvent event)
	{
		handleCustomPacket(event);
	}

	public void handleCustomPacket(CustomPacketEvent event)
	{
		if (DarkcoreMod.debugText) System.out.println("Packet received!");
		FMLProxyPacket p = event.packet;
		int length = p.payload().getByte(0);
		byte[] bytes = new byte[length];
		p.payload().readerIndex(1);
		p.payload().readBytes(bytes);
		p.payload().readerIndex(1+length);
		p.payload().discardReadBytes();
		String discriminator = new String(bytes);
		DataPacket dp = new DataPacket(p.payload());
		NBTTagCompound nbt = dp.getNBT();
		if (handlers.containsKey(discriminator))
			handlers.get(discriminator).handleData(nbt);
		else
			System.err.println("Packet with unknown discriminator " + discriminator + " received!");
	}

	public boolean registerHandler(String discriminator, IDataPacketHandler handler)
	{
		if (handlers.containsKey(discriminator))
		{
			IDataPacketHandler old = handlers.get(discriminator);
			throw new RuntimeException("Unable to register packet handler " + discriminator + " - conflict " + handler + ":" + old);
		}
		if(discriminator.getBytes().length > 255)
			throw new RuntimeException("Unable to register packet handler " + discriminator + " - name too long");
		handlers.put(discriminator, handler);
		return true;
	}
}
