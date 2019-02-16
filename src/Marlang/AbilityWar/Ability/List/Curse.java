package Marlang.AbilityWar.Ability.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import Marlang.AbilityWar.Ability.AbilityBase;
import Marlang.AbilityWar.Config.AbilitySettings.SettingObject;
import Marlang.AbilityWar.Utils.Messager;
import Marlang.AbilityWar.Utils.Library.SoundLib;

public class Curse extends AbilityBase {

	public static SettingObject<Integer> CountConfig = new SettingObject<Integer>("�ý�", "Count", 1,
			"# �ɷ� ���Ƚ��") {
		
		@Override
		public boolean Condition(Integer value) {
			return value >= 1;
		}
		
	};

	public Curse(Player player) {
		super(player, "�ý�", Rank.B,
				ChatColor.translateAlternateColorCodes('&', "&f������ ö���� Ÿ���ϸ� ������ �����ϰ� �ִ� ��� ���ʿ� �ͼ����ָ� �̴ϴ�."),
				ChatColor.translateAlternateColorCodes('&', "&f" + CountConfig.getValue() + "���� ����� �� �ֽ��ϴ�."));
	}
	
	private Integer Count = CountConfig.getValue();

	@Override
	public boolean ActiveSkill(ActiveMaterialType mt, ActiveClickType ct) {
		if(mt.equals(ActiveMaterialType.Iron_Ingot)) {
			if(ct.equals(ActiveClickType.LeftClick)) {
				if(Count > 0) {
					Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&a���&f�� �����ϴ�!"));
				} else {
					Messager.sendMessage(getPlayer(), ChatColor.translateAlternateColorCodes('&', "&c�̹� �ɷ��� ����Ͽ����ϴ�."));
				}
			}
		}
		
		return false;
	}

	@Override
	public void PassiveSkill(Event event) {
		if(event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			if(e.getDamager().equals(getPlayer())) {
				if(e.getEntity() instanceof Player) {
					Player p = (Player) e.getEntity();
					if(!e.isCancelled()) {
						if(getPlayer().getInventory().getItemInMainHand().getType().equals(Material.IRON_INGOT)) {
							if(Count > 0) {
								ItemStack Helmet = p.getInventory().getHelmet();
								if(Helmet != null) {
									Helmet.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
									p.getInventory().setHelmet(Helmet);
								}

								ItemStack Chestplate = p.getInventory().getChestplate();
								if(Chestplate != null) {
									Chestplate.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
									p.getInventory().setChestplate(Chestplate);
								}
								
								ItemStack Leggings = p.getInventory().getLeggings();
								if(Leggings != null) {
									Leggings.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
									p.getInventory().setLeggings(Leggings);
								}

								ItemStack Boots = p.getInventory().getBoots();
								if(Boots != null) {
									Boots.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
									p.getInventory().setBoots(Boots);
								}
								
								SoundLib.ENTITY_ELDER_GUARDIAN_CURSE.playSound(p);
								
								Count--;
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void onRestrictClear() {}

}