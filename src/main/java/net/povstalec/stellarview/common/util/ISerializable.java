package net.povstalec.stellarview.common.util;

import net.minecraft.nbt.CompoundTag;

public interface ISerializable
{
	CompoundTag serializeNBT();
	
	void deserializeNBT(CompoundTag tag);
}
