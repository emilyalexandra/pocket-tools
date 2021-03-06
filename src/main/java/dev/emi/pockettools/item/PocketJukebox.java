package dev.emi.pockettools.item;

import java.util.List;
import java.util.UUID;

import dev.emi.pockettools.sound.PocketRecordSoundInstance;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class PocketJukebox extends Item {

	public PocketJukebox(Settings settings) {
		super(settings);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		CompoundTag tag = stack.getOrCreateTag();
		if (world.isClient) {
			if (tag.contains("start") && tag.getInt("start") >= 1) {
				if (tag.contains("disc") && tag.contains("uuid") && entity instanceof PlayerEntity) {
					PocketRecordSoundInstance.activeUuid = tag.getUuid("uuid");
					if (PocketRecordSoundInstance.inst != null && !PocketRecordSoundInstance.inst.isDone()) {
						PocketRecordSoundInstance.inst.end();
					}
					ItemStack d = ItemStack.fromTag(tag.getCompound("disc"));
					MusicDiscItem disc = (MusicDiscItem) d.getItem();
					PocketRecordSoundInstance.inst = new PocketRecordSoundInstance(disc.getSound(), SoundCategory.RECORDS, (PlayerEntity) entity);
					PocketRecordSoundInstance.inst.start();
					PocketRecordSoundInstance.lastCheckin = 5;
				}
			}
			if (tag.contains("uuid") && tag.getUuid("uuid").equals(PocketRecordSoundInstance.activeUuid) && !PocketRecordSoundInstance.inst.isDone()) {
				PocketRecordSoundInstance.lastCheckin = 5;
			}
		} else {
			if (tag.contains("start") && tag.getInt("start") > 0) {
				tag.putInt("start", tag.getInt("start") - 1);
			}
		}
	}

	@Override
	public boolean onClicked(ItemStack self, ItemStack stack, Slot slot, ClickType clickType, PlayerInventory playerInventory) {
		CompoundTag tag = self.getOrCreateTag();
		if (clickType == ClickType.RIGHT) {
			PlayerEntity player = playerInventory.player;
			World world = player.world;
			if (tag.contains("disc")) {
				if (stack.isEmpty()) {
					playerInventory.setCursorStack(ItemStack.fromTag(tag.getCompound("disc")));
					if (world.isClient) {
						if (tag.contains("uuid") && tag.getUuid("uuid").equals(PocketRecordSoundInstance.activeUuid) && PocketRecordSoundInstance.inst != null && !PocketRecordSoundInstance.inst.isDone()) {
							PocketRecordSoundInstance.inst.end();
						}
					}
					tag.remove("start");
					tag.remove("uuid");
					tag.remove("disc");
					return true;
				}
			} else {
				if (stack.getItem() instanceof MusicDiscItem) {
					if (!world.isClient) {
						UUID uuid = UUID.randomUUID();
						tag.putUuid("uuid", uuid);
						tag.putInt("start", 2);
					}
					tag.put("disc", stack.toTag(new CompoundTag()));
					stack.decrement(1);
					return true;
				}
			}
		}
		return false;
	}

	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		CompoundTag tag = stack.getOrCreateTag();
		//boolean on = false;
		//if (tag.contains("uuid") && tag.getUuid("uuid").equals(activeUuid) && inst != null && !inst.isDone()) {
		//	on = true;
		//}
		if (tag.contains("disc")) {
			ItemStack disc = ItemStack.fromTag(tag.getCompound("disc"));
			MutableText text = ((MusicDiscItem) disc.getItem()).getDescription();
			//if (on) {
			//	//text = text.formatted(Formatting.byColorIndex((int) world.getTime() / 12 % 16));
			//	text = new TranslatableText("record.nowPlaying", new Object[]{text}).formatted(Formatting.LIGHT_PURPLE);
			//} else {
				text = text.formatted(Formatting.GRAY);
			//}
			tooltip.add(text);
		}
	}
}
